package net.coding.program.common.network;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import net.coding.program.R;
import net.coding.program.common.ui.BaseFragment;

/**
 * Created by chaochen on 14-10-7.
 * 封装了下拉刷新
 */
public abstract class RefreshBaseFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipeRefreshLayout;

    protected final boolean isRefreshing() {
        if (swipeRefreshLayout != null) {
            return swipeRefreshLayout.isRefreshing();
        }

        return false;
    }

    protected final void setRefreshing(boolean refreshing) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    protected final void initRefreshLayout() {
        View rootView = getView();
        if (rootView == null) {
            return;
        }

        View swipe = rootView.findViewById(R.id.swipeRefreshLayout);
        if (swipe == null) {
            return;
        }

        swipeRefreshLayout = (SwipeRefreshLayout) swipe;
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.font_green);
    }

    protected final void disableRefreshing() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(false);
        }
    }

    protected final void enableSwipeRefresh(boolean enable) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(enable);
        }
    }
}
