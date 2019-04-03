package net.coding.program.search;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.adapter.SearchHistoryListAdapter;
import net.coding.program.common.CodingColor;
import net.coding.program.common.Global;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.DensityUtil;
import net.coding.program.common.widget.input.SimpleTextWatcher;
import net.coding.program.third.WechatTab;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collections;

@EActivity(R.layout.activity_search_project)
public class SearchProjectActivity extends BackActivity implements TextView.OnEditorActionListener, AdapterView.OnItemClickListener {

    private static final String TAG = SearchProjectActivity.class.getSimpleName();

    @ViewById
    View emptyView;
    @ViewById
    WechatTab tabs;

    @ViewById(R.id.pager)
    ViewPager pager;
    @ViewById
    ListView emptyListView;
    @ViewById
    View allEmptyView;
    SearchHistoryListAdapter mSearchHistoryListAdapter;
    // footer
    private TextView mSearchFooterClearAllView;
    private View mSearchFooterDivider;
    private InputMethodManager imm;
    // 历史搜索的记录
    private ArrayList<String> mSearchHistoryList = new ArrayList<>();

    private String mSearchData = "";
    private EditText editText;
    private View btnCancel;

    @AfterViews
    void init() {
        View actionBar = getLayoutInflater().inflate(R.layout.activity_search_project_actionbar, null);
        getSupportActionBar().setCustomView(actionBar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        btnCancel = actionBar.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
            editText.setText("");
            Global.popSoftkeyboard(this, editText, true);
        });

        editText = actionBar.findViewById(R.id.editText);
        editText.requestFocus();

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);
        tabs.setTextSize(DensityUtil.dip2px(this, 16));
        tabs.setTabPaddingLeftRight(DensityUtil.dip2px(this, 20));
//        setTabsValue();

        emptyListView.setOnItemClickListener(this);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        initSearchFooterView();

        mSearchHistoryListAdapter = new SearchHistoryListAdapter(this, mSearchHistoryList);
        emptyListView.setAdapter(mSearchHistoryListAdapter);
        emptyListView.setOnItemClickListener(this);
        emptyListView.setVisibility(View.VISIBLE);
        loadSearchCache();
        editText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    emptyListView.setVisibility(View.VISIBLE);
                    pager.setVisibility(View.GONE);
                    tabs.setVisibility(View.GONE);
                    mSearchData = "";
                    loadSearchCache();
                    btnCancel.setVisibility(View.INVISIBLE);
                } else {
                    btnCancel.setVisibility(View.VISIBLE);
                }
            }
        });
        editText.setOnEditorActionListener(this);
    }

    private void setTabsValue() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        // 设置Tab是自动填充满屏幕的
        tabs.setShouldExpand(true);
        // 设置Tab的分割线是透明的
        tabs.setDividerColor(Color.TRANSPARENT);

//        tabs.setTextSelectedColor(this.getResources().getColor(R.color.user_info_tags_bg_2));
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
        tabs.setIndicatorColor(CodingColor.fontGreen);

        // 取消点击Tab时的背景色
        tabs.setTabBackground(0);
    }

    private void initSearchFooterView() {
        View footerView = LayoutInflater.from(this).inflate(R.layout.subject_search_history_list_footer, null);
        mSearchFooterClearAllView = footerView.findViewById(R.id.subject_search_hot_footer_clear);
        mSearchFooterClearAllView.setOnClickListener(v -> {
            AccountInfo.saveSearchProjectHistory(v.getContext(), new ArrayList<String>());
            loadSearchCache();
        });

        mSearchFooterDivider = footerView.findViewById(R.id.subject_search_hot_footer_divider);
        mSearchFooterDivider.setVisibility(View.GONE);
        emptyListView.addFooterView(footerView, null, false);
    }

    private void loadSearchCache() {
        mSearchHistoryList.clear();
        mSearchHistoryList.addAll(AccountInfo.loadSearchProjectHistory(this));
        notifySearchHistoryDataChanged();
    }

    private void notifySearchHistoryDataChanged() {
        showSearchClearView(mSearchHistoryList.size() > 0);
        mSearchHistoryListAdapter.notifyDataSetChanged();
        allEmptyView.setVisibility(mSearchHistoryList.size() > 0 ? View.GONE : View.VISIBLE);
    }

    private void showSearchClearView(boolean show) {
        int visable = show ? View.VISIBLE : View.GONE;
        mSearchFooterClearAllView.setVisibility(visable);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(0, 0);
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
//        tabs.setShouldExpand(false);
        setTabsValue();

        tabs.notifyDataSetChanged();
        editText.setText(condition);
        editText.setSelection(condition.length());

        int pos = mSearchHistoryList.indexOf(mSearchData);
        if (pos == -1) {

            if (mSearchHistoryList.size() >= 8) {
                mSearchHistoryList.remove(7);
            }
            mSearchHistoryList.add(0, mSearchData);
            AccountInfo.saveSearchProjectHistory(this, mSearchHistoryList);
        } else {
            if (pos > 0) {
                Collections.swap(mSearchHistoryList, 0, pos);
                AccountInfo.saveSearchProjectHistory(this, mSearchHistoryList);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        search(mSearchHistoryList.get(position));
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            String tmp = editText.getText().toString();
            if (TextUtils.isEmpty(tmp)) {
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

}
