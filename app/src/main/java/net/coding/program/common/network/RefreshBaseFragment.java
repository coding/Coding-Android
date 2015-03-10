package net.coding.program.common.network;

import android.support.v4.widget.SwipeRefreshLayout;

import net.coding.program.R;

/**
 * Created by chaochen on 14-10-7.
 */
public abstract class RefreshBaseFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String UPDATE_ALL = "99999999";
    public static final int UPDATE_ALL_INT = 99999999;

    SwipeRefreshLayout swipeRefreshLayout;

    protected void setRefreshing(boolean refreshing) {
        swipeRefreshLayout.setRefreshing(refreshing);
    }

    protected boolean isRefreshing() {
        return swipeRefreshLayout.isRefreshing();
    }

    protected void init() {
        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.green);
    }
}
