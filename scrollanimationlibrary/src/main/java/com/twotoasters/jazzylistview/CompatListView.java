package com.twotoasters.jazzylistview;

/**
 * Created by chenchao on 2016/12/13.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * Created by mkishan on 9/27/2015.
 */
public class CompatListView extends ListView implements NestedScrollingChild {
    private NestedScrollingChildHelper helper;
    private int mLastY;
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private int mNestedOffsetY;
    public CompatListView(Context context) {
        super(context);
        helper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
    }

    public CompatListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        helper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
    }

    public CompatListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        helper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CompatListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        helper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        helper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return helper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        helper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return helper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return helper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return helper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return helper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return helper.dispatchNestedPreFling(velocityX, velocityY);
    }

//    @Override
//    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
//        super.onNestedScroll(target, dxConsumed, dyUnconsumed, dxUnconsumed, dyConsumed);
//    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        boolean rs = false;
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            mNestedOffsetY = 0;
        }
        int y = (int) event.getY();
        event.offsetLocation(0,mNestedOffsetY);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                rs = super.onTouchEvent(event);
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:

                int dy = mLastY - y;
                int oldY = getScrollY();
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);

                if (dispatchNestedPreScroll(0, dy, mScrollConsumed, mScrollOffset)) {
                    dy -= mScrollConsumed[1];
                    event.offsetLocation(0,-mScrollOffset[1]);
                    mNestedOffsetY += mScrollOffset[1];
                }
                rs = super.onTouchEvent(event);
                mLastY  = y - mScrollOffset[1];
                if (dy < 0) {
                    int newScrollY = Math.max(0, oldY+dy);
                    dy -= newScrollY - oldY;
                    if (dispatchNestedScroll(0, newScrollY - dy, 0, dy, mScrollOffset)) {
                        event.offsetLocation(0,mScrollOffset[1]);
                        mNestedOffsetY += mScrollOffset[1];
                        mLastY -= mScrollOffset[1];
                    }
                }
                Log.d("dy", dy + "");
                Log.d("oldy", oldY+"");
                Log.d("mlasty", mLastY+"");
                stopNestedScroll();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                rs = super.onTouchEvent(event);
//                mLastY = 0;
//                mNestedOffsetY = 0;
                stopNestedScroll();
                break;
        }
        return rs;
    }
}