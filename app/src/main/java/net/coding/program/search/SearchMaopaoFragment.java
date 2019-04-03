package net.coding.program.search;

import android.view.View;
import android.widget.AbsListView;

import net.coding.program.R;
import net.coding.program.adapter.SearchMaopaoAdapter;
import net.coding.program.common.Global;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.model.Maopao;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Vernon on 15/11/28.
 */
@EFragment(R.layout.fragment_search_list)
public class SearchMaopaoFragment extends SearchBaseFragment {
    final String url = Global.HOST_API + "/esearch/all?q=%s";
    final String tmp = "&types=%s&pageSize=10";
    @InstanceState
    protected String keyword = "";
    String page = "&page=%s";
    int pos = 1;
    ArrayList<Maopao.MaopaoObject> mData = new ArrayList<>();
    SearchMaopaoAdapter adapter;
    private String tabPrams;
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
                    pos++;
                    isLoading = true;
                    loadMore();
                }
            }
        }
    };
    private MyImageGetter myImageGetter;

    @AfterViews
    protected void init() {
        initRefreshLayout();
        myImageGetter = new MyImageGetter(getActivity());
        setRefreshing(true);
        mFootUpdate.init(listView, mInflater, this);

        adapter = new SearchMaopaoAdapter(mData, myImageGetter, getActivity());
        listView.setAdapter(adapter);
        listView.setOnScrollListener(mOnScrollListener);
        loadMore();
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
        tag = String.format(url, getKeyword()) + String.format(tmp, getTabPrams()) + String.format(page, pos + "");
        return tag;
    }


    @Override
    public void loadMore() {
        getNetwork(getUrl(pos), keyword);
    }

    @Override
    public void onRefresh() {
//        pos = 1;
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
                JSONArray array = respanse.getJSONObject("data").getJSONObject("tweets").getJSONArray("list");
                for (int i = 0; i < array.length(); ++i) {
                    JSONObject item = array.getJSONObject(i);
                    Maopao.MaopaoObject oneData = new Maopao.MaopaoObject(item);
                    mData.add(oneData);
                }
                emptyView.setVisibility(mData.size() == 0 ? View.VISIBLE : View.GONE);
                if (array.length() > 0) {
                    hasMore = true;
                    mFootUpdate.updateState(code, false, mData.size());
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
