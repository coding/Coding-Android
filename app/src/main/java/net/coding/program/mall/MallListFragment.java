package net.coding.program.mall;

import android.graphics.Rect;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
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

    String mDataUrl = Global.HOST_API + "/gifts?pageSize=20";

    private MyRecyclerAdapter mAdapter;

    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };

    @AfterViews
    void initView() {
        initRefreshLayout();

        if (mType.equals(Type.all_goods)) {
            mDataUrl = Global.HOST_API + "/gifts?pageSize=20";
        } else {
            mDataUrl = Global.HOST_API + "/gifts?pageSize=20";
        }
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2,
                GridLayoutManager.VERTICAL, false);
        mallListHeaderGridView.setLayoutManager(layoutManager);
        mallListHeaderGridView.setItemAnimator(new DefaultItemAnimator());

        //item间距
        int space = getContext().getResources()
                .getDimensionPixelSize(R.dimen.shop_list_item_vertical_space);
        mallListHeaderGridView.addItemDecoration(new SpaceItemDecoration(space));

        mAdapter = new MyRecyclerAdapter(mData, userPoint, getImageLoad(), getActivity());
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
//                    mAdapter = new MyRecyclerAdapter(mData, userPoint, getImageLoad(),
//                            getActivity());
//                    mallListHeaderGridView.setAdapter(mAdapter);
                    mAdapter.setUserPoint(userPoint);
                    mAdapter.removeAll();
                }

                JSONArray jsonArray = respanse.optJSONObject("data").optJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    MallItemObject orderObject = new MallItemObject(json);
                    if (mType == Type.all_goods) {
                        mData.add(orderObject);
                    } else if (mType == Type.can_change) {
                        if (orderObject.getPoints_cost() <= userPoint) {
                            mData.add(orderObject);
                        }
                    }
                }

                String tip = BlankViewDisplay.OTHER_MALL_EXCHANGE_BLANK;
                BlankViewDisplay.setBlank(mData.size(), this, true, blankLayout, onClickRetry, tip);
                mAdapter.removeAll();
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
        getNextPageNetwork(mDataUrl, mDataUrl);
    }

    @Override
    public void loadMore() {
        if (!isLoadingLastPage(mDataUrl)) {
            showDialogLoading();
        }
        getNextPageNetwork(mDataUrl, mDataUrl);
    }

    public enum Type {
        all_goods,
        can_change
    }

    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {

            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space / 2;

//            if(parent.getChildLayoutPosition(view) == 0)
//                outRect.top = space;
        }
    }
}
