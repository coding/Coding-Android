package net.coding.program.project.detail;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.Global;
import net.coding.program.common.LoadMore;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.model.DynamicObject;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.network.model.user.Member;
import net.coding.program.project.DateSectionDynamicAdapter;
import net.coding.program.route.BlankViewDisplay;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;

@EFragment(R.layout.fragment_project_dynamic)
public class ProjectDynamicFragment extends RefreshBaseFragment implements LoadMore {

    final String HOST = Global.HOST_API + "/project/%d/activities?last_id=%s&user_id=%s&type=%s";
    final String HOST_USER = Global.HOST_API + "/project/%d/activities/user/%s?last_id=%s";
    final String TAG_PROJECT_DYNMAIC = "TAG_PROJECT_DYNMAIC";
    @FragmentArg
    protected ProjectObject mProjectObject;
    @FragmentArg
    protected String mType;
    @FragmentArg
    protected int mUser_id;
    @FragmentArg
    protected Member mMember;
    @ViewById
    protected View blankLayout;
    @ViewById
    protected ExpandableStickyListHeadersListView listView;
    int mLastId = Global.UPDATE_ALL_INT;
    boolean mNoMore = false;
    ArrayList<DynamicObject.DynamicBaseObject> mData = new ArrayList<>();
    ProjectDynamicAdapter mAdapter;
    String sToday = "";
    String sYesterday = "";
    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };
    private LayoutInflater inflater;
    private SimpleDateFormat mDataDyanmicItem = new SimpleDateFormat("HH:mm");
    private MyImageGetter myImageGetter;
    private LoadingAnimation mLoadingAnimation;

    public static DynamicObject.DynamicBaseObject getDynamicObject(JSONObject json, int projectId) throws JSONException {
        String itemType = json.optString("target_type");
        DynamicObject.DynamicBaseObject baseObject;

        if (itemType.equals("ProjectMember")) {
            baseObject = new DynamicObject.DynamicProjectMember(json);

        } else if (itemType.equals("Depot")) { // 项目分支
            baseObject = new DynamicObject.DynamicDepotPush(json);

        } else if (itemType.equals("Task")) {
            baseObject = new DynamicObject.DynamicTask(json);

        } else if (itemType.equals("ProjectFile")) {
            baseObject = new DynamicObject.DynamicProjectFile(json).projectId(projectId);

        } else if (itemType.equals("QcTask")) {
            baseObject = new DynamicObject.DynamicQcTask(json);

        } else if (itemType.equals("ProjectTopic")) {
            baseObject = new DynamicObject.DynamicProjectTopic(json);

        } else if (itemType.equals("Project")) {
            baseObject = new DynamicObject.DynamicProject(json);

        } else if (itemType.equals("ProjectStar")) {
            baseObject = new DynamicObject.ProjectStar(json);

        } else if (itemType.equals("ProjectWatcher")) {
            baseObject = new DynamicObject.ProjectWatcher(json);

        } else if (itemType.equals("PullRequestComment")) {
            baseObject = new DynamicObject.PullRequestComment(json);

        } else if (itemType.equals("PullRequestBean")) {
            baseObject = new DynamicObject.PullRequestBean(json);

        } else if (itemType.equals("MergeRequestComment")) {
            baseObject = new DynamicObject.MergeRequestComment(json);

        } else if (itemType.equals("MergeRequestBean")) {
            baseObject = new DynamicObject.MergeRequestBean(json);

        } else if (itemType.equals("ProjectTweet")) {
            baseObject = new DynamicObject.ProjectTweet(json);

        } else if (itemType.equals("TaskComment")) {
            baseObject = new DynamicObject.MyTaskComment(json);

        } else if (itemType.equals("CommitLineNote")) {
            baseObject = new DynamicObject.CommitLineNote(json);

        } else if (itemType.equals("ProjectFileComment")) {
            baseObject = new DynamicObject.DynamicProjectFileComment(json);

        } else if (itemType.equals("Wiki")) {
            baseObject = new DynamicObject.Wiki(json);

        } else if (itemType.equals("ProtectedBranch")) {
            baseObject = new DynamicObject.ProtectedBranch(json);

        } else if (itemType.equals("BranchMember")) {
            baseObject = new DynamicObject.BranchMember(json);

        } else if (itemType.equals("Release")) {
            baseObject = new DynamicObject.Release(json);

        } else if (itemType.equals("Milestone")) {
            baseObject = new DynamicObject.Milestone(json);

        } else {
            Log.e("", "新的动态类型 " + itemType);
            baseObject = new DynamicObject.DynamicBaseObject(json);
        }
        return baseObject;
    }

    protected void destoryLoadingAnimation() {
        if (mLoadingAnimation != null) {
            mLoadingAnimation.destory();
            mLoadingAnimation = null;
        }
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setHasOptionsMenu(true);
        mAdapter = new ProjectDynamicAdapter(getContext(), myImageGetter, this);
    }

    @AfterViews
    final protected void init() {
        initRefreshLayout();

        mLoadingAnimation = new LoadingAnimation();
        mLoadingAnimation.startAnimation();

        myImageGetter = new MyImageGetter(getActivity());

        inflater = LayoutInflater.from(getActivity());

        Calendar calendar = Calendar.getInstance();
        Long today = calendar.getTimeInMillis();
        sToday = Global.mDateFormat.format(today);
        Long yesterday = calendar.getTimeInMillis() - 1000 * 60 * 60 * 24;
        sYesterday = Global.mDateFormat.format(yesterday);

        mLastId = Global.UPDATE_ALL_INT;

        listView.setDividerHeight(0);
        mFootUpdate.init(listView, mInflater, this);
        listView.setAdapter(mAdapter);
        loadMore();
    }

    @Override
    public void loadMore() {
        mLastId = mAdapter.lastId();
        String getUrl;
        if (mUser_id == 0) {
            getUrl = String.format(HOST, mProjectObject.getId(), mLastId, mProjectObject.owner_id, mType);
        } else {
            getUrl = String.format(HOST_USER, mProjectObject.getId(), mUser_id, mLastId);
        }

        getNetwork(getUrl, TAG_PROJECT_DYNMAIC, 0, mLastId);
    }

    @Override
    public void onRefresh() {
        mAdapter.resetLastId();
        mLastId = mAdapter.lastId();
        loadMore();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_PROJECT_DYNMAIC)) {
            if (((int) data) == Global.UPDATE_ALL_INT) {
                if (mLoadingAnimation != null) {
                    mLoadingAnimation.destory();
                    mLoadingAnimation = null;
                }

                setRefreshing(false);

                if (code == 0) {
                    mData.clear();
                }
            }

            if (code == 0) {
                JSONArray array = respanse.getJSONArray("data");

                for (int i = 0; i < array.length(); ++i) {
                    JSONObject json = array.getJSONObject(i);

                    DynamicObject.DynamicBaseObject baseObject = getDynamicObject(json, mProjectObject.getId());

                    mData.add(baseObject);
                }


                if (array.length() == 0) {
                    mNoMore = true;
                    mAdapter.setHasMore(false);
                    mFootUpdate.dismiss();

                } else {
                    mNoMore = false;
                    mAdapter.setHasMore(true);
                    mFootUpdate.showLoading();
                }

                BlankViewDisplay.setBlank(mData.size(), this, true, blankLayout, onClickRetry);

                mAdapter.resetData(mData);
            } else {
                if (mData.isEmpty()) {
                    mFootUpdate.dismiss();
                } else {
                    mFootUpdate.showFail();
                }
                showErrorMsg(code, respanse);

                BlankViewDisplay.setBlank(mData.size(), this, false, blankLayout, onClickRetry);
            }
        }
    }

    protected int getListSectionResourceId() {
        return R.layout.fragment_project_dynamic_list_head;
    }

    class LoadingAnimation {

        View v;

        public LoadingAnimation() {
            v = getActivity().getLayoutInflater().inflate(R.layout.loading_view, null);

            ((ViewGroup) getView()).addView(v);
        }

        public void startAnimation() {
        }

        public void destory() {
            ((ViewGroup) getView()).removeView(v);
        }
    }

    private class ProjectDynamicAdapter extends DateSectionDynamicAdapter {
        public ProjectDynamicAdapter(Context context, MyImageGetter imageGetter, LoadMore loader) {
            super(context, imageGetter, loader);
        }

        @Override
        public void afterGetView(int position, View convertView, ViewGroup parent, DateSectionDynamicAdapter.ViewHolder holder) {
            super.afterGetView(position, convertView, parent, holder);
            if (position < mProjectObject.unReadActivitiesCount) {
                holder.timeLinePoint.getDelegate().setBackgroundColor(CodingColor.fontGreen);
            } else {
                holder.timeLinePoint.getDelegate().setBackgroundColor(0xFFD8DDE4);
            }

            if (position == mProjectObject.unReadActivitiesCount - 1) {
                holder.divideLeft.setVisibility(View.VISIBLE);
                holder.timeLineDown.setVisibility(View.INVISIBLE);
            } else {
                holder.divideLeft.setVisibility(View.INVISIBLE);
            }

            if (position == mProjectObject.unReadActivitiesCount) {
                holder.timeLineUp.setVisibility(View.INVISIBLE);
            }
        }
    }
}
