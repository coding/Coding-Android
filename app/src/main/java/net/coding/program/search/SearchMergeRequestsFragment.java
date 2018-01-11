package net.coding.program.search;

import android.view.View;
import android.widget.AbsListView;

import net.coding.program.R;
import net.coding.program.adapter.SearchMergeAdapter;
import net.coding.program.common.Global;
import net.coding.program.common.model.Merge;
import net.coding.program.project.detail.merge.MergeDetailActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Vernon on 15/11/30.
 */
@EFragment(R.layout.fragment_search_list)
public class SearchMergeRequestsFragment extends SearchBaseFragment {
    private static final String TAG = SearchTaskFragment.class.getSimpleName();
    final String url = Global.HOST_API + "/esearch/%s?q=%s";
    final String tmp = "&types=%s&pageSize=10";
    @InstanceState
    protected String tabPrams;
    ArrayList<Merge> mData = new ArrayList<>();
    String page = "&page=%s";
    int pos = 1;
    SearchMergeAdapter adapter;
    private String keyword = "";
    private boolean hasMore = true;
    private boolean isLoading = true;
    AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem + visibleItemCount == totalItemCount) {
                if (hasMore && !isLoading) {
                    isLoading = true;
                    pos++;
                    loadMore();
                }
            }
        }
    };

    @AfterViews
    protected void init() {
        initRefreshLayout();
        setRefreshing(true);
        mFootUpdate.init(listView, mInflater, this);
        adapter = new SearchMergeAdapter(mData, keyword, getActivity());
        listView.setAdapter(adapter);
        listView.setOnScrollListener(mOnScrollListener);
        loadMore();
    }

    @ItemClick
    final void listView(Merge itemData) {
        if (itemData.getMergeStatus() == Merge.Status.CANCEL) {
            return;
        }

        String url = String.format("%s%s", Global.HOST_API, itemData.getPath());
        MergeDetailActivity_.intent(this).mMergeUrl(url).start();
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getTabPrams() {
        return tabPrams;
    }

    public void setTabPrams(String tabPrams) {
        this.tabPrams = tabPrams;
    }

    private String getUrl(int pos) {
        String tag = "";
        String type = tabPrams.equals(SearchFramgentAdapter.MERGE_REQUEST) ? "mr" : "pr";
        tag = String.format(url, type, getKeyword()) + String.format(tmp, getTabPrams()) + String.format(page, pos + "");
        return tag;
    }

    @Override
    public void loadMore() {
        getNetwork(getUrl(pos), keyword);
    }

    @Override
    public void onRefresh() {
        loadMore();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(keyword)) {
            setRefreshing(false);
            if (code == 0) {
                if (pos == 1) {
                    mData.clear();
                }
                JSONArray array = respanse.getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < array.length(); ++i) {
                    JSONObject item = array.getJSONObject(i);
                    Merge oneData = new Merge(item);
                    mData.add(oneData);
                }
                emptyView.setVisibility(mData.size() == 0 ? View.VISIBLE : View.GONE);
                if (array.length() > 0) {
                    mFootUpdate.updateState(code, false, mData.size());
                    hasMore = true;
                } else {
                    hasMore = false;
                    mFootUpdate.updateState(code, true, mData.size());
                }
                adapter.notifyDataSetChanged();
                isLoading = false;
            } else {
                showErrorMsg(code, respanse);
                hasMore = false;
            }
        }
    }
}
