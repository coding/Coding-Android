package net.coding.program.maopao;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.coding.program.BackActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.SearchCache;
import net.coding.program.model.Subject;
import net.coding.program.subject.SubjectSearchFragment;
import net.coding.program.subject.adapter.SubjectSearchHistoryListAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 15-7-20.
 */
@EActivity(R.layout.activity_search_maopao)
public class MaopaoSearchActivity extends BackActivity {
    @ViewById
    View container;
    @ViewById
    ListView emptyListView;
    RelativeLayout mSearchHotTitle;
    FlowLayout mSearchHotLayout;
    SubjectSearchHistoryListAdapter mSearchHistoryListAdapter;

    private String mHotTweetUrl = "/tweet_topic/hot?page=1&pageSize=6";
    SearchView editText;
    private SubjectSearchFragment searchFragment;
    // 热门话题列表的数据
    private List<Subject.SubjectDescObject> mSubjectList = new ArrayList<Subject.SubjectDescObject>();
    // 历史搜索的记录
    private List<String> mSearchHistoryList = new ArrayList<String>();


    private String mSearchData = "";

    @AfterViews
    void init() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        actionBar.setCustomView(R.layout.activity_search_project_actionbar);

        editText = (SearchView) findViewById(R.id.editText);
        editText.onActionViewExpanded();
        editText.setIconified(false);

        editText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (s == null || TextUtils.isEmpty(s)) {
                    emptyListView.setVisibility(View.VISIBLE);
                    container.setVisibility(View.INVISIBLE);
                    mSearchData = "";
                } else {
                    mSearchData = s;
                    emptyListView.setVisibility(View.INVISIBLE);
                    container.setVisibility(View.VISIBLE);
                    updateSearchResult();
                    SearchCache.getInstance(MaopaoSearchActivity.this).add(mSearchData);
                    loadSearchCache();
                }

                getSupportActionBar().setTitle(R.string.title_activity_search_project);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
//                mSearchData.clear();
//                if (s.length() > 0) {
//                    String enter = s.toString().toLowerCase();
//                    for (ProjectObject item : mData) {
//                        if (item.name.toLowerCase().contains(enter) ||
//                                item.owner_user_name.toLowerCase().contains(enter) ||
//                                Html.fromHtml(item.owner_user_home).toString().toLowerCase().contains(enter)) {
//                            mSearchData.add(item);
//                        }
//                    }
//                }
//
//                if (mSearchData.isEmpty()) {
//                    emptyView.setVisibility(View.VISIBLE);
//                    container.setVisibility(View.INVISIBLE);
//                } else {
//                    emptyView.setVisibility(View.INVISIBLE);
//                    container.setVisibility(View.VISIBLE);
//                    updateSearchResult();
//                }
                if (s == null || TextUtils.isEmpty(s)) {
                    emptyListView.setVisibility(View.VISIBLE);
                    container.setVisibility(View.INVISIBLE);
                    mSearchData = "";
                }
//                else {
//                    mSearchData = s;
//                    emptyListView.setVisibility(View.INVISIBLE);
//                    container.setVisibility(View.VISIBLE);
//                    updateSearchResult();
//                }
//
//                getSupportActionBar().setTitle(R.string.title_activity_search_project);
                return true;
            }
        });

        initSearchHeaderView();
        mSearchHistoryListAdapter = new SubjectSearchHistoryListAdapter(this, mSearchHistoryList);
        emptyListView.setAdapter(mSearchHistoryListAdapter);

        searchFragment = new SubjectSearchFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, searchFragment)
                .commit();
        loadHotSubject();
        loadSearchCache();
    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    private void updateSearchResult() {
        searchFragment.updateData(mSearchData);
    }

    void initSearchHeaderView() {
        View headerView = LayoutInflater.from(this).inflate(R.layout.subject_search_history_list_header, null);
        mSearchHotTitle = (RelativeLayout) headerView.findViewById(R.id.subject_search_hot_header_title);
        mSearchHotLayout = (FlowLayout) headerView.findViewById(R.id.subject_search_hot_layout);
        mSearchHotLayout.setOnClickListener(mOnClickListener);
        emptyListView.addHeaderView(headerView);

    }

    private void loadSearchCache() {
        mSearchHistoryList.clear();
        mSearchHistoryList.addAll(SearchCache.getInstance(this).getSearchCacheList());
        mSearchHistoryListAdapter.notifyDataSetChanged();
    }

    private void loadHotSubject() {
        showDialogLoading();
        getNetwork(Global.HOST_API + mHotTweetUrl, mSearchData);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if ("".equals(tag)) {
            hideProgressDialog();
            JSONArray jsonArray = respanse.optJSONArray("data");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    Subject.SubjectDescObject projectObject = new Subject.SubjectDescObject(json);
                    mSubjectList.add(projectObject);
                }
            }
            if (mSubjectList != null) {
                fillHotTweetToLayout();
            }
        }
    }

    private void fillHotTweetToLayout() {
        if (mSubjectList != null) {
            Subject.SubjectDescObject descObject = null;
            for (int i = 0; i < mSubjectList.size(); i++) {
                descObject = mSubjectList.get(i);
                TextView textView = new TextView(this);
                textView.setText("#" + descObject.name + "#");
                ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.bottomMargin = 54;
                textView.setLayoutParams(params);
                mSearchHotLayout.addView(textView);
                if (i > 5)
                    break;
            }
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.subject_search_hot_header_title:
                    // TODO：跳转到热门搜索
                    break;
            }
        }
    };
}
