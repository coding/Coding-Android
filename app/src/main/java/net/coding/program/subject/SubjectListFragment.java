package net.coding.program.subject;

import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.model.Subject;
import net.coding.program.subject.adapter.SubjectListItemAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 15-7-18.
 */
@EFragment(R.layout.fragment_subject_list)
public class SubjectListFragment extends RefreshBaseFragment {

    final String subjectFollowUrlFormat = Global.HOST_API + "/user/%s/tweet_topic/watched?extraInfo=1";
    final String subjectJoinUrlFormat = Global.HOST_API + "/user/%s/tweet_topic/joined?extraInfo=1";
    final String subjectHotTweetUrl = Global.HOST_API + "/tweet_topic/hot?page=1&pageSize=20";
    final String subjectFollowTag = "subject_follow";
    final String subjectJoinTag = "subject_join";
    final String subjectHotTweetTag = "subject_hot";

    @FragmentArg
    Type mType;
    @FragmentArg
    String userKey;

    @ViewById
    ListView listView;
    @ViewById
    View blankLayout;

    SubjectListItemAdapter mAdapter = null;
    private List<Subject.SubjectDescObject> mSubjectList = new ArrayList<Subject.SubjectDescObject>();

    private MyImageGetter mMyImageGetter;

    @AfterViews
    protected void init() {
        initRefreshLayout();

        setRefreshing(true);

        if (mType != Type.hot)
            mFootUpdate.init(listView, mInflater, this);

        mMyImageGetter = new MyImageGetter(getActivity());

        mAdapter = new SubjectListItemAdapter(getActivity(), mSubjectList, mMyImageGetter);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(onItemClickListener);
        listView.setOnScrollListener(mOnScrollListener);
        loadMore();
    }

    AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            if (firstVisibleItem + visibleItemCount >= totalItemCount) {
                loadMore();
            }
        }
    };


    public enum Type {
        follow, join, hot
    }

    @Override
    public void onRefresh() {
        initSetting();
        loadMore();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(getCurTag())) {
            setRefreshing(false);
            if (code == 0) {
                if (isLoadingFirstPage(tag)) {
                    mSubjectList.clear();
                }
                JSONArray jsonArray = null;
                if (mType == Type.hot) {
                    jsonArray = respanse.optJSONArray("data");
                } else {
                    jsonArray = respanse.optJSONObject("data").optJSONArray("list");
                }
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    Subject.SubjectDescObject projectObject = new Subject.SubjectDescObject(json);
                    mSubjectList.add(projectObject);
                }
                if (mType != Type.hot)
                    mFootUpdate.updateState(code, isLoadingLastPage(tag), mSubjectList.size());

                String tip = BlankViewDisplay.OTHER_SUBJECT_BLANK;
                if (userKey.equals(MyApp.sUserObject.global_key)) {
                    tip = BlankViewDisplay.MY_SUBJECT_BLANK;
                }
                BlankViewDisplay.setBlank(mSubjectList.size(), this, true, blankLayout, null, tip);

                mAdapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @Override
    public void loadMore() {
        if (mType == Type.hot) {
            getNetwork(getUrl(), getCurTag());
        } else
            getNextPageNetwork(getUrl(), getCurTag());
    }

    private String getUrl() {
        String url = "";
        if (mType == Type.follow) {
            url = String.format(subjectFollowUrlFormat, userKey);
        } else if (mType == Type.join) {
            url = String.format(subjectJoinUrlFormat, userKey);
        } else if (mType == Type.hot) {
            url = subjectHotTweetUrl;
        }
        return url;
    }

    private String getCurTag() {
        String tag = "";

        if (mType == Type.follow) {
            tag = subjectFollowTag;
        } else if (mType == Type.join) {
            tag = subjectJoinTag;
        } else if (mType == Type.hot) {
            tag = subjectHotTweetTag;
        }
        return tag;
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int pos = position;
            if (pos >= 0 && pos < mSubjectList.size()) {
                SubjectDetailActivity_.intent(getActivity()).subjectDescObject(mSubjectList.get(pos)).start();
            }
        }
    };

}



