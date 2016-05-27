package net.coding.program.search;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.common.util.DensityUtil;
import net.coding.program.R;
import net.coding.program.common.SearchProjectCache;
import net.coding.program.common.adapter.SearchHistoryListAdapter;
import net.coding.program.common.ui.BaseActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_search_project)
public class SearchProjectActivity extends BaseActivity implements TextView.OnEditorActionListener, TextWatcher, View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = SearchProjectActivity.class.getSimpleName();
    @ViewById
    View emptyView;
    @ViewById
    net.coding.program.common.PagerSlidingTabStrip tabs;
    @ViewById(R.id.pager)
    ViewPager pager;
    // footer
    private TextView mSearchFooterClearAllView;
    private View mSearchFooterDivider;
    @ViewById
    ListView emptyListView;

    private InputMethodManager imm;

    SearchHistoryListAdapter mSearchHistoryListAdapter;
    // 历史搜索的记录
    private List<String> mSearchHistoryList = new ArrayList<String>();
    private String mSearchData = "";
    private Button btnCancel;
    private EditText editText;

    @AfterViews
    void init() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        btnCancel = (Button) this.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);
        editText = (EditText) this.findViewById(R.id.editText);
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);
        tabs.setTextSize(DensityUtil.dip2px(this, 16));
        tabs.setTabPaddingLeftRight(DensityUtil.dip2px(this, 20));
        setTabsValue();
        emptyListView.setOnItemClickListener(this);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        initSearchFooterView();


        mSearchHistoryListAdapter = new SearchHistoryListAdapter(this, mSearchHistoryList);
        emptyListView.setAdapter(mSearchHistoryListAdapter);
        emptyListView.setOnItemClickListener(this);
        emptyListView.setVisibility(View.VISIBLE);
        loadSearchCache();
        editText.addTextChangedListener(this);
        editText.setOnEditorActionListener(this);

    }

    private void setTabsValue() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        // 设置Tab是自动填充满屏幕的
        tabs.setShouldExpand(true);
        // 设置Tab的分割线是透明的
        tabs.setDividerColor(Color.TRANSPARENT);

        tabs.setTextSelectedColor(this.getResources().getColor(R.color.user_info_tags_bg_2));
        // 设置Tab底部线的高度
        tabs.setUnderlineHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, dm));
        // 设置Tab Indicator的高度
        tabs.setIndicatorHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 3, dm));
        // 设置Tab标题文字的大小
        tabs.setTextSize((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 16, dm));
        // 设置Tab Indicator的颜色
        tabs.setIndicatorColor(Color.parseColor("#3bbd79"));

        // 取消点击Tab时的背景色
        tabs.setTabBackground(0);
    }

    private void initSearchFooterView() {
        View footerView = LayoutInflater.from(this).inflate(R.layout.subject_search_history_list_footer, null);
        mSearchFooterClearAllView = (TextView) footerView.findViewById(R.id.subject_search_hot_footer_clear);
        mSearchFooterClearAllView.setOnClickListener(mOnClickListener);
        mSearchFooterDivider = footerView.findViewById(R.id.subject_search_hot_footer_divider);
        mSearchFooterDivider.setVisibility(View.GONE);
        emptyListView.addFooterView(footerView, null, false);
    }

    private void loadSearchCache() {
        mSearchHistoryList.clear();
        mSearchHistoryList.addAll(SearchProjectCache.getInstance(this).getSearchCacheList());
        notifySearchHistoryDataChanged();
    }

    private void notifySearchHistoryDataChanged() {
        if (mSearchHistoryList.size() > 0)
            showSearchClearView();
        else
            hideSearchClearView();
        mSearchHistoryListAdapter.notifyDataSetChanged();
    }

    private void showSearchClearView() {
        mSearchFooterClearAllView.setVisibility(View.VISIBLE);
    }

    private void hideSearchClearView() {
        mSearchFooterClearAllView.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.
                overridePendingTransition(0, 0);
    }

    private void search(String condition) {
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        mSearchData = condition;
        emptyListView.setVisibility(View.GONE);
        tabs.setVisibility(View.VISIBLE);
        pager.setVisibility(View.VISIBLE);
        pager.setAdapter(new SearchFramgentAdapter(getSupportFragmentManager(), condition));
        pager.setOffscreenPageLimit(8);
        tabs.setViewPager(pager);
        tabs.notifyDataSetChanged();
        editText.setText(condition);
        editText.setSelection(condition.length());
        updateSearchResult();
        SearchProjectCache.getInstance(SearchProjectActivity.this).add(mSearchData);
    }

    private void updateSearchResult() {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        search(mSearchHistoryList.get(position));
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.subject_search_hot_footer_clear:
                    SearchProjectCache.getInstance(SearchProjectActivity.this).clearCache();
                    loadSearchCache();
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel:
                onBackPressed();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            String tmp = editText.getText().toString();
            if (tmp == null || TextUtils.isEmpty(tmp)) {
                emptyListView.setVisibility(View.VISIBLE);
                pager.setVisibility(View.GONE);
                tabs.setVisibility(View.GONE);
                mSearchData = "";
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            } else {
                search(tmp);
            }
            return true;
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s == null || TextUtils.isEmpty(s)) {
            emptyListView.setVisibility(View.VISIBLE);
            pager.setVisibility(View.GONE);
            tabs.setVisibility(View.GONE);
            mSearchData = "";
            loadSearchCache();
        }
    }
}
