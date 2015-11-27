package net.coding.program.mall;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.network.RefreshBaseAppCompatFragment;
import net.coding.program.model.MallItemObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by libo on 2015/11/25.
 */
@EFragment(R.layout.fragment_mall_index_list)
public class MallListFragment extends RefreshBaseAppCompatFragment {

    @ViewById
    RecyclerView mallListHeaderGridView;

    @ViewById
    View blankLayout;

    @FragmentArg
    Type mType;

    boolean isSlidingToLast = false;

    final String USER_POINT_URL = Global.HOST_API + "/account/points";

    double userPoint = 0.0;

    ArrayList<MallItemObject> mData = new ArrayList<>();

    String mDataUrl = Global.HOST_API + "/gifts?";

    //    private MallListAdapter mAdapter;
    private MyRecylerAdapter mAdapter;

    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };

    @AfterViews
    void initView() {
        initRefreshLayout();
        mFootUpdate.init(mallListHeaderGridView, mInflater, this);

        if (mType.equals(Type.all_goods)) {
            mDataUrl = Global.HOST_API + "/gifts?";
        } else {
            mDataUrl = Global.HOST_API + "/gifts?";
        }
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2,
                GridLayoutManager.VERTICAL, false);
        mallListHeaderGridView.setLayoutManager(layoutManager);
        mAdapter = new MyRecylerAdapter(mData, userPoint, getImageLoad(), getActivity());
        mallListHeaderGridView.setAdapter(mAdapter);

        //到底加载更多
        mallListHeaderGridView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastVisableItem = manager.findLastCompletelyVisibleItemPosition();
                    int totalItemCount = manager.getItemCount();
                    if (lastVisableItem == (totalItemCount - 1) && isSlidingToLast) {
                        //加载更多
                        loadMore();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    isSlidingToLast = true;
                } else {
                    isSlidingToLast = false;
                }
            }
        });
        getNetwork(USER_POINT_URL, USER_POINT_URL);
//        loadMore();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data)
            throws JSONException {
        hideProgressDialog();
        setRefreshing(false);
        if (tag.equals(USER_POINT_URL)) {
            if (code == 0) {
                JSONObject jsonObject = respanse.getJSONObject("data");
                userPoint = jsonObject.optDouble("points_left");

                onRefresh();
            } else {
                showErrorMsg(code, respanse);
            }
        }
        if (tag.equals(mDataUrl)) {

            if (code == 0) {
                if (isLoadingFirstPage(tag)) {
                    mData.clear();
                    mAdapter = new MyRecylerAdapter(mData, userPoint, getImageLoad(),
                            getActivity());
                    mallListHeaderGridView.setAdapter(mAdapter);
                }

                JSONArray jsonArray = respanse.optJSONObject("data").optJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    synchronized (mData) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        MallItemObject orderObject = new MallItemObject(json);
                        if (mType == Type.all_goods) {
                            mData.add(orderObject);
                        } else if (mType == Type.can_change) {
                            if (orderObject.getPoints_cost() < userPoint) {
                                mData.add(orderObject);
                            }
                        }
                    }
                }

//                mFootUpdate.updateState(code, isLoadingLastPage(tag), mData.size());
//                String tip = BlankViewDisplay.OTHER_MALL_ORDER_BLANK;
//                BlankViewDisplay.setBlank(mData.size(), this, true, blankLayout, onClickRetry, tip);

                mAdapter.addAll(mData);
                mAdapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @Override
    public void onRefresh() {
        initSetting();
        loadMore();
//        getNetwork(mDataUrl, mDataUrl);
    }

    @Override
    public void loadMore() {
        if (isLoadingLastPage(mDataUrl)){
            return;
        }
        setRefreshing(true);
        getNextPageNetwork(mDataUrl, mDataUrl);
    }

    public enum Type {
        all_goods,
        can_change
    }

}
