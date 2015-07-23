package net.coding.program.maopao;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.coding.program.BackActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.SearchCache;
import net.coding.program.model.Subject;
import net.coding.program.subject.SubjectSearchFragment;
import net.coding.program.subject.SubjectWallActivity;
import net.coding.program.subject.SubjectWallActivity_;
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

    // footer
    private TextView mSearchFooterClearAllView;
    private View mSearchFooterDivider;

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

        actionBar.setCustomView(R.layout.activity_search_maopao_actionbar);

        editText = (SearchView) findViewById(R.id.editText);
        editText.setQueryHint(Html.fromHtml("<font color = #80ffffff>搜索冒泡、用户名、话题</font>"));
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
                }

                getSupportActionBar().setTitle(R.string.title_activity_search_project);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s == null || TextUtils.isEmpty(s)) {
                    emptyListView.setVisibility(View.VISIBLE);
                    container.setVisibility(View.INVISIBLE);
                    mSearchData = "";
                    loadSearchCache();
                }
                return true;
            }
        });

        initSearchHeaderView();
        initSearchFooterView();
        mSearchHistoryListAdapter = new SubjectSearchHistoryListAdapter(this, mSearchHistoryList);
        emptyListView.setAdapter(mSearchHistoryListAdapter);

        searchFragment = new SubjectSearchFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, searchFragment)
                .commit();
        loadSearchCache();
        loadHotSubject();
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
        mSearchHotTitle.setOnClickListener(mOnClickListener);
        mSearchHotLayout = (FlowLayout) headerView.findViewById(R.id.subject_search_hot_layout);
        emptyListView.addHeaderView(headerView);

    }

    private void initSearchFooterView() {
        View footerView = LayoutInflater.from(this).inflate(R.layout.subject_search_history_list_footer, null);
        mSearchFooterClearAllView = (TextView) footerView.findViewById(R.id.subject_search_hot_footer_clear);
        mSearchFooterClearAllView.setOnClickListener(mOnClickListener);
        mSearchFooterDivider = footerView.findViewById(R.id.subject_search_hot_footer_divider);
        emptyListView.addFooterView(footerView);
    }

    private void showSearchClearView() {
        mSearchFooterDivider.setVisibility(View.VISIBLE);
        mSearchFooterClearAllView.setVisibility(View.VISIBLE);
    }

    private void hideSearchClearView() {
        mSearchFooterDivider.setVisibility(View.GONE);
        mSearchFooterClearAllView.setVisibility(View.GONE);
    }

    private void loadSearchCache() {
        mSearchHistoryList.clear();
        mSearchHistoryList.addAll(SearchCache.getInstance(this).getSearchCacheList());
        notifySearchHistoryDataChanged();
    }

    private void notifySearchHistoryDataChanged() {
        if (mSearchHistoryList.size() > 0)
            showSearchClearView();
        else
            hideSearchClearView();
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
            emptyListView.setVisibility(View.VISIBLE);
        }
    }

    private void fillHotTweetToLayout() {
        if (mSubjectList != null) {
            Subject.SubjectDescObject descObject = null;
            View itemView = null;
            TextView textView = null;
            for (int i = 0; i < mSubjectList.size(); i++) {
                descObject = mSubjectList.get(i);
                itemView = LayoutInflater.from(this).inflate(R.layout.subject_search_hot_topic_item, null);
                textView = (TextView) itemView.findViewById(R.id.hot_tweet_item);
                textView.setText("#" + descObject.name + "#");
                if (i == 0) {
                    textView.setBackground(getResources().getDrawable(R.drawable.round_green_corner));
                    textView.setTextColor(getResources().getColor(R.color.merge_green));
                } else {
                    textView.setBackground(getResources().getDrawable(R.drawable.round_gray_corner));
                    textView.setTextColor(getResources().getColor(R.color.font_black_2));
                }
                mSearchHotLayout.addView(itemView);
                if (i > 4)
                    break;
            }
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.subject_search_hot_header_title:
                    SubjectWallActivity_.intent(MaopaoSearchActivity.this).start();
                    break;
                case R.id.subject_search_hot_footer_clear:
                    SearchCache.getInstance(MaopaoSearchActivity.this).clearCache();
                    loadSearchCache();
                    break;
            }
        }
    };

    private int getPxValue(float dipValue) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, getResources().getDisplayMetrics()) + 0.5f);
    }
}
