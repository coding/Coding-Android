package net.coding.program.message;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

/**
 * Created by chaochen on 15/2/9.
 */
public class TouchLinearLayout extends RelativeLayout {

    private long pressTime = 0;
    private int action;

    public TouchLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            pressTime = System.currentTimeMillis();
        } else if (action == MotionEvent.ACTION_UP) {
            if (System.currentTimeMillis() - pressTime < ViewConfiguration.getLongPressTimeout()) {
                performClick();
            }
        }

        return super.dispatchTouchEvent(ev);
    }
}
