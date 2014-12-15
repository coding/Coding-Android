package net.coding.program.common.network;

import android.support.v4.widget.SwipeRefreshLayout;

import net.coding.program.R;

/**
 * Created by chaochen on 14-10-7.
 */
public abstract class RefreshBaseFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String UPDATE_ALL = "99999999";

    SwipeRefreshLayout swipeRefreshLayout;

    private void initSwipeLayout(SwipeRefreshLayout swipeLayout) {
        swipeLayout.setOnRefreshListener(this);
//        swipeLayout.setColorSchemeResources(R.color.green, android.R.color.holo_red_light, android.R.color.holo_blue_light, android.R.color.holo_orange_light);
//        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
//                android.R.color.holo_green_light,
//                android.R.color.holo_orange_light,
//                android.R.color.holo_red_light);
        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.transparent,
                android.R.color.holo_blue_light,
                android.R.color.transparent
        );
    }

    protected void setRefreshing(boolean refreshing) {
        swipeRefreshLayout.setRefreshing(refreshing);
    }

    protected boolean isRefreshing() {
        return swipeRefreshLayout.isRefreshing();
    }

    protected void init() {
        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
        initSwipeLayout(swipeRefreshLayout);
    }
}
