package net.coding.program.subject;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.loopj.android.http.RequestParams;

import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.model.UserObject;
import net.coding.program.subject.adapter.SubjectUserListAdapter;
import net.coding.program.user.UserDetailActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 15-7-28.
 */
@EFragment(R.layout.fragment_subject_list)
public class SubjectUserFragment extends RefreshBaseFragment implements FootUpdate.LoadMore{
    public static final String HOST_FOLLOW = Global.HOST_API + "/user/follow?";
    public static final String HOST_UNFOLLOW = Global.HOST_API + "/user/unfollow?";
    final String subjectUserListUrlFormat = Global.HOST_API + "/tweet_topic/%s/joined?pageSize=10";
    final String subjectUserListTag = "subject_user_list_tag";

    static final int RESULT_USER_DETAIL = 5;

    @FragmentArg
    int topicId;

    @ViewById
    ListView listView;
    @ViewById
    View blankLayout;

    SubjectUserListAdapter mAdapter = null;
    private List<UserObject> mUserList = new ArrayList<>();

    @AfterViews
    protected void init() {
        initRefreshLayout();

        setRefreshing(true);

        mFootUpdate.init(listView, mInflater, this);

        mAdapter = new SubjectUserListAdapter(getActivity(), mUserList);
        mAdapter.setFollowClickListener(mFollowClickListener);
        listView.setAdapter(mAdapter);
        listView.setOnScrollListener(mOnScrollListener);
        loadMore();
    }


    private AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if(firstVisibleItem + visibleItemCount >= totalItemCount){
                loadMore();
            }
        }
    };

    @Override
    public void onRefresh() {
        initSetting();
        loadMore();
    }

    @ItemClick
    final void listView(UserObject itemData) {
        UserDetailActivity_.intent(this).globalKey(itemData.global_key).startForResult(RESULT_USER_DETAIL);
    }

    @OnActivityResult(RESULT_USER_DETAIL)
    final void resultUserDetail(int result, Intent data) {
        if (result == Activity.RESULT_OK) {
            UserObject userObject = (UserObject) data.getSerializableExtra("data");
            for (int i = 0; i < mUserList.size(); ++i) {
                if (mUserList.get(i).id == userObject.id) {
                    mUserList.remove(i);
                    mUserList.add(i, userObject);
                    mAdapter.notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(subjectUserListTag)) {
            setRefreshing(false);
            if (code == 0) {
                if (isLoadingFirstPage(tag)) {
                    mUserList.clear();
                }
                JSONArray jsonArray = respanse.optJSONObject("data").optJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    UserObject projectObject = new UserObject(json);
                    mUserList.add(projectObject);
                }
                mFootUpdate.updateState(code, isLoadingLastPage(tag), mUserList.size());

//                String tip = BlankViewDisplay.OTHER_PROJECT_BLANK;
//                if (mUserObject.isMe()) {
//                    tip = BlankViewDisplay.MY_PROJECT_BLANK;
//                }
//                BlankViewDisplay.setBlank(mData.size(), this, true, blankLayout, onClickRetry, tip);

                mAdapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_FOLLOW)) {
            if (code == 0) {
                showButtomToast(R.string.follow_success);
                mUserList.get(pos).followed = true;
            } else {
                showButtomToast(R.string.follow_fail);
            }
            mAdapter.notifyDataSetChanged();
        } else if (tag.equals(HOST_UNFOLLOW)) {
            if (code == 0) {
                showButtomToast(R.string.unfollow_success);
                mUserList.get(pos).followed = false;
            } else {
                showButtomToast(R.string.unfollow_fail);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void loadMore() {
        getNextPageNetwork(getUrl(), subjectUserListTag);
    }

    private String getUrl() {
        return String.format(subjectUserListUrlFormat, topicId);
    }

    private View.OnClickListener mFollowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = Integer.valueOf(v.getTag().toString());
            UserObject data = mUserList.get(pos);
            if (data != null) {
                RequestParams params = new RequestParams();
                params.put("users", data.global_key);
                if (((CheckBox) v).isChecked()) {
                    postNetwork(HOST_FOLLOW, params, HOST_FOLLOW, pos, null);
                } else {
                    postNetwork(HOST_UNFOLLOW, params, HOST_UNFOLLOW, pos, null);
                }
            }
        }
    };
}
