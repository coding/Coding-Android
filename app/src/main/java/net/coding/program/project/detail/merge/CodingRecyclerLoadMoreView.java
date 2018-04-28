package net.coding.program.project.detail.merge;


import com.chad.library.adapter.base.loadmore.LoadMoreView;

import net.coding.program.R;

public final class CodingRecyclerLoadMoreView extends LoadMoreView {

    boolean showShadow = true;

    public CodingRecyclerLoadMoreView() {
        this(true);
    }

    public CodingRecyclerLoadMoreView(boolean showShadow) {
        this.showShadow = showShadow;
    }

    @Override
    public int getLayoutId() {
        return showShadow ?
                R.layout.view_load_more :
                R.layout.view_load_more_no_shadow;
    }

    @Override
    protected int getLoadingViewId() {
        return R.id.load_more_loading_view;
    }

    @Override
    protected int getLoadFailViewId() {
        return R.id.load_more_load_fail_view;
    }

    @Override
    protected int getLoadEndViewId() {
        return R.id.load_more_load_end_view;
    }
}
