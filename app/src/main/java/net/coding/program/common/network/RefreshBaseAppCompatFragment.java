package net.coding.program.common.network;

import android.support.v4.widget.SwipeRefreshLayout;

import net.coding.program.R;
import net.coding.program.common.ui.BaseFragment;

/**
 * Created by libo on 2015/11/25.
 */
public abstract class RefreshBaseAppCompatFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String UPDATE_ALL = "999999999";
    public static final int UPDATE_ALL_INT = 999999999;

    private SwipeRefreshLayout swipeRefreshLayout;

    protected final boolean isRefreshing() {
        return swipeRefreshLayout.isRefreshing();
    }

    protected final void setRefreshing(boolean refreshing) {
        swipeRefreshLayout.setRefreshing(refreshing);
    }

    protected final void initRefreshLayout() {
        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.green);
    }

    protected final void disableRefreshing() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(false);
        }
    }

    protected final void enableSwipeRefresh(boolean enable) {
        swipeRefreshLayout.setEnabled(enable);
    }
}
