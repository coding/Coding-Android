package net.coding.program.subject.loop;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by david on 15/4/20.
 */
public class AutoScrollLoopViewPager extends LoopViewPager {

    public AutoScrollLoopViewPager(Context context) {
        super(context);
    }

    public AutoScrollLoopViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private static final int INTERVAL = 5000;
    private boolean isPagingEnabled = true;

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mAutoScrollAction);
    }

    private void sendScrollMessage() {
        postDelayed(mAutoScrollAction, INTERVAL);
    }

    private Runnable mAutoScrollAction = new Runnable() {
        @Override
        public void run() {
            sendScrollMessage();
            goForwardSmoothly();
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                removeCallbacks(mAutoScrollAction);
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                sendScrollMessage();
                break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void startAutoScroll() {
        sendScrollMessage();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.isPagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.isPagingEnabled && super.onInterceptTouchEvent(event);
    }

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }
}
