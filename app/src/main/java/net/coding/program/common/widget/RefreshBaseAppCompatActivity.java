package net.coding.program.common.widget;

import net.coding.program.R;
import net.coding.program.common.ui.BaseAppCompatActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.support.v4.widget.SwipeRefreshLayout;

/**
 * Created by libo on 2015/11/25.
 */
@EActivity
public abstract class RefreshBaseAppCompatActivity extends BaseAppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    public static final String UPDATE_ALL = "99999999";
    public static final int UPDATE_ALL_INT = 99999999;

    @ViewById
    protected SwipeRefreshLayout swipeRefreshLayout;

    protected final boolean isRefreshing() {
        return swipeRefreshLayout.isRefreshing();
    }

    protected final void setRefreshing(boolean refreshing) {
        swipeRefreshLayout.setRefreshing(refreshing);
    }

    @AfterViews
    protected final void initRefreshBaseActivity() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.green);
    }

    protected final void disableRefreshing() {
        swipeRefreshLayout.setEnabled(false);
    }
}
