package net.coding.program.search;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AbsListView;

import net.coding.program.R;
import net.coding.program.adapter.SearchTopicAdapter;
import net.coding.program.common.Global;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.model.TopicObject;
import net.coding.program.network.HttpObserverRaw;
import net.coding.program.network.Network;
import net.coding.program.network.model.HttpResult;
import net.coding.program.project.detail.topic.TopicListDetailActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Vernon on 15/11/27.
 */
@EFragment(R.layout.fragment_search_list)
public class SearchCommentFragment extends SearchBaseFragment {

    private static final String TAG = SearchTaskFragment.class.getSimpleName();
    final String url = Global.HOST_API + "/esearch/all?q=%s";
    final String tmp = "&types=%s&pageSize=10";
    String page = "&page=%s";
    int pos = 1;
    ArrayList<TopicObject> mData = new ArrayList<>();
    SearchTopicAdapter adapter;
    private String keyword = "";
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
        adapter = new SearchTopicAdapter(mData, getActivity(), keyword);
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

    @ItemClick
    final void listView(TopicObject itemData) {
        showProgressBar(true);

        Network.getRetrofit(getActivity())
                .getProject(itemData.project_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserverRaw<HttpResult<ProjectObject>>(getActivity()) {
                    @Override
                    public void onSuccess(HttpResult<ProjectObject> data) {
                        super.onSuccess(data);
                        showProgressBar(false);

                        TopicListDetailActivity_.intent(getActivity()).topicObject(itemData).projectObject(data.data).start();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        showProgressBar(false);
                    }
                });

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
                JSONArray array = respanse.getJSONObject("data").getJSONObject("project_topics").getJSONArray("list");
                for (int i = 0; i < array.length(); ++i) {
                    JSONObject item = array.getJSONObject(i);
                    TopicObject oneData = new TopicObject(item);
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