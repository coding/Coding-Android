package net.coding.program.search;

import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.adapter.SearchMergeAdapter;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.model.Merge;
import net.coding.program.model.MergeObject;
import net.coding.program.project.detail.merge.MergeDetailActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Vernon on 15/11/30.
 */
@EFragment(R.layout.fragment_search_list)
public class SearchMergeRequestsFragment extends RefreshBaseFragment {
    private static final String TAG = SearchTaskFragment.class.getSimpleName();
    final String url = Global.HOST_API + "/esearch/all?q=%s";
    final String tmp = "&types=%s&pageSize=10";
    ArrayList<MergeObject> mData = new ArrayList<>();
    String page = "&page=%s";
    int pos = 1;
    private String keyword = "";
    private String tabPrams;
    private boolean hasMore = true;
    private boolean isLoading = true;
    @ViewById
    ListView listView;
    @ViewById(R.id.emptyView)
    LinearLayout emptyView;

    SearchMergeAdapter adapter;

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

    @ItemClick
    final void listView(MergeObject itemData) {
        Merge merge = new Merge();
        merge.setId(itemData.getId());
        merge.setSrcBranch(itemData.getSrcBranch());
        merge.setDesBranch(itemData.getDesBranch());
        merge.setCreated_at(itemData.getCreatedAt());
        merge.setAuthor(itemData.getAuthor());
        merge.setAction_at(itemData.getAction_at());
        merge.setMerge_status(itemData.merge_status);
        merge.setContent(itemData.getBody());
        merge.setIid(itemData.getIid());
        merge.setTitle(itemData.getTitle());
        merge.setPath(itemData.getPath());
        Merge.ActionAuthor userObject = new Merge.ActionAuthor();
        userObject.name = itemData.getAuthor().name;
        merge.setAction_author(userObject);
        MergeDetailActivity_.intent(this).mMerge(merge).start();
    }

    public String getKeyword() {
        return keyword;
    }

    public String getTabPrams() {
        return tabPrams;
    }

    public void setTabPrams(String tabPrams) {
        this.tabPrams = tabPrams;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    private String getUrl(int pos) {
        String tag = "";
        tag = String.format(url, getKeyword()) + String.format(tmp, getTabPrams()) + String.format(page, pos + "");
        return tag;
    }

    @Override
    public void loadMore() {
        getNetwork(getUrl(pos), keyword);
    }

    @Override
    public void onRefresh() {
        pos = 1;
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
                JSONArray array;
                if (getUrl(pos).contains("merge_requests")) {
                    array = respanse.getJSONObject("data").getJSONObject("merge_requests").getJSONArray("list");
                } else {
                    array = respanse.getJSONObject("data").getJSONObject("pull_requests").getJSONArray("list");
                }
                for (int i = 0; i < array.length(); ++i) {
                    JSONObject item = array.getJSONObject(i);
                    MergeObject oneData = new MergeObject(item);
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
