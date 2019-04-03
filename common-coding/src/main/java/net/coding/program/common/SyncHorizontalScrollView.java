package net.coding.program.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;

/**
 * Created by anfs on 2016/11/28.
 * 横向 HorizontalScrollView 跟随
 */
public class SyncHorizontalScrollView extends HorizontalScrollView {
    private View mView;

    public SyncHorizontalScrollView(Context context) {
        this(context, null);
    }

    public SyncHorizontalScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SyncHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mView != null) {
            mView.scrollTo(l, t);
        }
    }

    public void setScrollView(View view) {
        mView = view;
    }

}
