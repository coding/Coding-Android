package net.coding.program.subject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.model.Subject;
import net.coding.program.subject.adapter.SubjectSearchHistoryListAdapter;
import net.coding.program.subject.adapter.SubjectSearchListAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;

/**
 * Created by david on 15-7-21.
 */
@EFragment(R.layout.subject_search_fragment)
public class SubjectSearchFragment extends BaseFragment {

    @ViewById
    ListView listView;
    SubjectSearchListAdapter mSubjectSearchListAdapter;

  //  @ViewById
//    ListView emptylistView;
//    RelativeLayout mSearchHotTitle;
//    FlowLayout mSearchHotLayout;
//    SubjectSearchHistoryListAdapter mSearchHistoryListAdapter;
//
//    private String mHotTweetUrl = "/tweet_topic/hot?page=1&pageSize=6";


    @ViewById
    View blankLayout;

    // 当前的搜索条件
    private String mCondition = "";
    // 热门话题列表的数据
    private List<Subject.SubjectDescObject> mSubjectList = new ArrayList<Subject.SubjectDescObject>();
    // 历史搜索的记录
    private List<String> mSearchHistoryList = new ArrayList<String>();

    public void updateData(String condition){
        mCondition = condition;
    }

    @AfterViews
    void init() {
//        mSearchHistoryListAdapter = new SubjectSearchHistoryListAdapter(getActivity(), mSearchHistoryList);
//        initSearchHeaderView();
//        emptylistView.setAdapter(mSearchHistoryListAdapter);
//
////        mSubjectSearchListAdapter = new SubjectSearchListAdapter(getActivity())
//
//        loadHotSubject();
    }

//    void initSearchHeaderView() {
//        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.subject_search_history_list_header, null);
//        mSearchHotTitle = (RelativeLayout) headerView.findViewById(R.id.subject_search_hot_header_title);
//        mSearchHotLayout = (FlowLayout) headerView.findViewById(R.id.subject_search_hot_layout);
//        emptylistView.addHeaderView(headerView);
//
//    }
//
//    private void loadHotSubject() {
//        showDialogLoading();
//        getNetwork(mHotTweetUrl, mCondition);
//    }
//
//    @Override
//    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
//        if ("".equals(tag)) {
//            hideProgressDialog();
//            JSONArray jsonArray = respanse.optJSONObject("data").optJSONArray("list");
//            for (int i = 0; i < jsonArray.length(); ++i) {
//                JSONObject json = jsonArray.getJSONObject(i);
//                Subject.SubjectDescObject projectObject = new Subject.SubjectDescObject(json);
//                mSubjectList.add(projectObject);
//            }
//            if (mSubjectList != null) {
//                fillHotTweetToLayout();
//            }
//        }
//    }
//
//    private void fillHotTweetToLayout() {
//        if (mSubjectList != null) {
//            Subject.SubjectDescObject descObject = null;
//            for (int i = 0; i < mSubjectList.size(); i++) {
//                descObject = mSubjectList.get(i);
//                TextView textView = new TextView(getActivity());
//                textView.setText("#" + descObject.name + "#");
//                ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                params.bottomMargin = 54;
//                textView.setLayoutParams(params);
//                mSearchHotLayout.addView(textView);
//            }
//        }
//    }
}
