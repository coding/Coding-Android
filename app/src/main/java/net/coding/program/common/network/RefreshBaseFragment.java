package net.coding.program.common.network;

import android.support.v4.widget.SwipeRefreshLayout;

import net.coding.program.R;

/**
 * Created by chaochen on 14-10-7.
 */
public abstract class RefreshBaseFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String UPDATE_ALL = "99999999";
    public static final int UPDATE_ALL_INT = 99999999;

    private SwipeRefreshLayout swipeRefreshLayout;

    protected final void setRefreshing(boolean refreshing) {
        swipeRefreshLayout.setRefreshing(refreshing);
    }

    protected final boolean isRefreshing() {
        return swipeRefreshLayout.isRefreshing();
    }

    protected final void initRefreshLayout() {
        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.green);
    }
}
