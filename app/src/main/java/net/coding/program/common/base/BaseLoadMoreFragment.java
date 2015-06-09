package net.coding.program.common.base;

import android.widget.ListView;

import net.coding.program.FootUpdate;
import net.coding.program.common.network.RefreshBaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by chenchao on 15/6/1.
 */
@EFragment
public abstract class BaseLoadMoreFragment extends RefreshBaseFragment implements FootUpdate.LoadMore {

    @ViewById
    protected ListView listView;

    private FootUpdate mFootUpdate = new FootUpdate();

    @AfterViews
    protected final void initBaseLoadMoreFragment() {
        mFootUpdate.init(listView, mInflater, this);
    }

    // parseJson 里面必须调用，用来刷新上拉刷新view的状态
    protected final void updateLoadingState(int code, String tag, int size) {
        mFootUpdate.updateState(code, isLoadingLastPage(tag), size);
    }
}
