package net.coding.program.project.detail;


import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.LongClickLinkMovementMethod;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.base.CustomMoreFragment;
import net.coding.program.common.htmltext.URLSpanNoUnderline;
import net.coding.program.model.DynamicObject;
import net.coding.program.model.ProjectObject;
import net.coding.program.model.TaskObject;

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
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

@EFragment(R.layout.fragment_project_dynamic)
public class ProjectDynamicFragment extends CustomMoreFragment implements FootUpdate.LoadMore {

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
    protected TaskObject.Members mMember;
    @ViewById
    protected View blankLayout;
    @ViewById
    protected ExpandableStickyListHeadersListView listView;
    int mLastId = UPDATE_ALL_INT;
    boolean mNoMore = false;
    ArrayList<DynamicObject.DynamicBaseObject> mData = new ArrayList<>();
    ProjectDynamicAdapter mAdapter = new ProjectDynamicAdapter();
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

    public static boolean isDifferentDay(Calendar c1, Calendar c2) {
        return (c1.get(Calendar.DAY_OF_MONTH) != c2.get(Calendar.DAY_OF_MONTH))
                || (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH))
                || (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR));
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

        mLastId = UPDATE_ALL_INT;

        listView.setDividerHeight(0);
        mFootUpdate.init(listView, mInflater, this);
        listView.setAdapter(mAdapter);

        loadMore();
    }

    @Override
    public void loadMore() {
        String getUrl;
        if (mUser_id == 0) {
            getUrl = String.format(HOST, mProjectObject.getId(), mLastId, mProjectObject.owner_id, mType);
        } else {
            getUrl = String.format(HOST_USER, mProjectObject.getId(), mUser_id, mLastId);
        }

        getNetwork(getUrl, TAG_PROJECT_DYNMAIC, 0, mLastId);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mMember != null) {
            inflater.inflate(R.menu.common_more, menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onRefresh() {
        mLastId = UPDATE_ALL_INT;
        loadMore();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_PROJECT_DYNMAIC)) {
            if (((int) data) == UPDATE_ALL_INT) {
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

                    String itemType = json.getString("target_type");
                    DynamicObject.DynamicBaseObject baseObject;

                    if (itemType.equals("ProjectMember")) {
                        baseObject = new DynamicObject.DynamicProjectMember(json);

                    } else if (itemType.equals("Depot")) { // 项目分支
                        baseObject = new DynamicObject.DynamicDepotPush(json);

                    } else if (itemType.equals("Task")) {
                        baseObject = new DynamicObject.DynamicTask(json);

                    } else if (itemType.equals("ProjectFile")) {
                        baseObject = new DynamicObject.DynamicProjectFile(json).projectId(mProjectObject.getId());

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

                    } else if (itemType.equals("TaskComment")) {
                        baseObject = new DynamicObject.MyTaskComment(json);

                    } else if (itemType.equals("CommitLineNote")) {
                        baseObject = new DynamicObject.CommitLineNote(json);

                    } else if (itemType.equals("ProjectFileComment")) {
                        baseObject = new DynamicObject.DynamicProjectFileComment(json);

                    } else {
                        Log.e("", "新的动态类型 " + itemType);
                        baseObject = new DynamicObject.DynamicBaseObject(json);
                    }

                    mData.add(baseObject);
                }


                if (array.length() == 0) {
                    mNoMore = true;
                    mFootUpdate.dismiss();

                } else {
                    mNoMore = false;

                    mFootUpdate.showLoading();
                }

                BlankViewDisplay.setBlank(mData.size(), this, true, blankLayout, onClickRetry);

                mAdapter.initSection();
                mAdapter.notifyDataSetChanged();
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

    @Override
    protected String getLink() {
        return mProjectObject.getPath() + "/members/" + mMember.user.global_key;
    }

    class LoadingAnimation {

        ImageView loadingLogo;
        ImageView loadingRound;
        View v;
        private Animation loadingLogoAnimation;
        private Animation loadingRoundAnimation;

        public LoadingAnimation() {
            v = getActivity().getLayoutInflater().inflate(R.layout.common_loading, null);
            this.loadingLogo = (ImageView) v.findViewById(R.id.loading_logo);
            this.loadingRound = (ImageView) v.findViewById(R.id.loading_round);

            loadingLogoAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.loading_alpha);
            loadingRoundAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.loading_rotate);

            ((ViewGroup) getView()).addView(v);
        }

        public void startAnimation() {
            loadingRoundAnimation.setStartTime(500L);//不然会跳帧
            loadingRound.setAnimation(loadingRoundAnimation);
            loadingLogo.startAnimation(loadingLogoAnimation);
        }

        public void destory() {
            ((ViewGroup) getView()).removeView(v);
        }
    }

    private class ProjectDynamicAdapter extends BaseAdapter implements
            StickyListHeadersAdapter, SectionIndexer {

        View.OnClickListener onClickJump = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = (String) v.getTag();
                if (s.isEmpty()) {
                    return;
                }

                URLSpanNoUnderline.openActivityByUri(v.getContext(), s, false);
            }
        };
        View.OnClickListener onClickParent = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ViewGroup) v.getParent()).callOnClick();
            }
        };
        private ArrayList<Long> mSectionTitle = new ArrayList<>();
        private ArrayList<Integer> mSectionId = new ArrayList<>();

        public ProjectDynamicAdapter() {
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        public void initSection() {
            mSectionTitle.clear();
            mSectionId.clear();

            if (mData.size() > 0) {
                mSectionId.add(0);
                Calendar lastTime = Calendar.getInstance();
                lastTime.setTimeInMillis(mData.get(0).created_at);
                Calendar nowTime = Calendar.getInstance();
                mSectionTitle.add(lastTime.getTimeInMillis());

                for (int i = 0; i < mData.size(); ++i) {
                    nowTime.setTimeInMillis(mData.get(i).created_at);
                    if (isDifferentDay(lastTime, nowTime)) {
                        lastTime.setTimeInMillis(nowTime.getTimeInMillis());
                        mSectionTitle.add(lastTime.getTimeInMillis());
                        mSectionId.add(i);
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.fragment_project_dynamic_list_item, parent, false);
                holder.mTitle = (TextView) convertView.findViewById(R.id.title);
                holder.mTitle.setMovementMethod(LongClickLinkMovementMethod.getInstance());
                holder.mTitle.setFocusable(false);

                holder.mContent = (TextView) convertView.findViewById(R.id.comment);
                holder.mContent.setMovementMethod(LongClickLinkMovementMethod.getInstance());
                holder.mContent.setOnClickListener(onClickParent);
                holder.mContent.setFocusable(false);

                holder.mLayoutClick = (ViewGroup) convertView.findViewById(R.id.layout0);
                holder.mLayoutClick.setOnClickListener(onClickJump);

                holder.mIcon = (ImageView) convertView.findViewById(R.id.icon);
                holder.mIcon.setOnClickListener(mOnClickUser);

                holder.mTime = (TextView) convertView.findViewById(R.id.time);
                holder.timeLineUp = convertView.findViewById(R.id.timeLineUp);
                holder.timeLinePoint = convertView.findViewById(R.id.timeLinePoint);
                holder.timeLineDown = convertView.findViewById(R.id.timeLineDown);
                holder.divideLeft = convertView.findViewById(R.id.divideLeft);
                holder.divideRight = convertView.findViewById(R.id.divideRight);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            DynamicObject.DynamicBaseObject data = (DynamicObject.DynamicBaseObject) getItem(position);
            holder.mTitle.setText(data.title());
            Spanned contentSpanned = data.content(myImageGetter);
            if (contentSpanned.length() == 0) {
                holder.mContent.setVisibility(View.GONE);
            } else {
                holder.mContent.setVisibility(View.VISIBLE);
                holder.mContent.setText(data.content(myImageGetter));
            }
            holder.mTime.setText(mDataDyanmicItem.format(data.created_at));

            holder.mLayoutClick.setTag(data.jump());

            if (position < mProjectObject.un_read_activities_count) {
                holder.timeLinePoint.setBackgroundResource(R.drawable.ic_dynamic_timeline_new);
            } else {
                holder.timeLinePoint.setBackgroundResource(R.drawable.ic_dynamic_timeline_old);
            }

            int nowSection = getSectionForPosition(position);
            if (position == 0) {
                holder.timeLineUp.setVisibility(View.INVISIBLE);
            } else {
                int upItemSection = getSectionForPosition(position - 1);
                if (nowSection == upItemSection) {
                    holder.timeLineUp.setVisibility(View.VISIBLE);
                } else {
                    holder.timeLineUp.setVisibility(View.INVISIBLE);
                }
            }

            if (position == mData.size() - 1) {
                if (mNoMore) {
                    holder.timeLineDown.setVisibility(View.INVISIBLE);
                } else {
                    holder.timeLineDown.setVisibility(View.VISIBLE);
                }
            } else {
                int downItemSection = getSectionForPosition(position + 1);
                if (nowSection == downItemSection) {
                    holder.timeLineDown.setVisibility(View.VISIBLE);
                } else {
                    holder.timeLineDown.setVisibility(View.INVISIBLE);
                }
            }

            if (position == mProjectObject.un_read_activities_count - 1) {
                holder.divideLeft.setVisibility(View.VISIBLE);
                holder.timeLineDown.setVisibility(View.INVISIBLE);
            } else {
                holder.divideLeft.setVisibility(View.INVISIBLE);
            }

            if (position == mProjectObject.un_read_activities_count) {
                holder.timeLineUp.setVisibility(View.INVISIBLE);
            }

            iconfromNetwork(holder.mIcon, data.user.avatar);
            holder.mIcon.setTag(data.user.global_key);

            if (mData.size() - position <= 3) {
                if (!mNoMore) {
                    int lastId = mData.get(mData.size() - 1).id;
                    if (mLastId != (lastId)) {
                        mLastId = lastId;
                        loadMore();
                    }
                }
            }

            return convertView;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = inflater.inflate(getListSectionResourceId(), parent, false);
                holder.mHead = (TextView) convertView.findViewById(R.id.head);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }

            Long time = mSectionTitle.get(getSectionForPosition(position));
            String s = Global.mDateFormat.format(time);
            if (s.equals(sToday)) {
                s += " (今天)";
            } else if (s.equals(sYesterday)) {
                s += " (昨天)";
            }

            holder.mHead.setText(s);

            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            return getSectionForPosition(position);
        }

        @Override
        public int getPositionForSection(int section) {
            return section;
        }

        @Override
        public int getSectionForPosition(int position) {
            for (int i = 0; i < mSectionId.size(); ++i) {
                if (position < mSectionId.get(i)) {
                    return i - 1;
                }
            }

            return mSectionId.size() - 1;
        }

        @Override
        public Object[] getSections() {
            return mSectionTitle.toArray();
        }

        class HeaderViewHolder {
            TextView mHead;
        }

        class ViewHolder {
            ImageView mIcon;
            TextView mTitle;
            TextView mContent;
            TextView mTime;

            ViewGroup mLayoutClick;

            View timeLineUp;
            View timeLinePoint;
            View timeLineDown;

            View divideLeft;
            View divideRight;
        }
    }
}
