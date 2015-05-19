package net.coding.program.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Created by chenchao on 15/5/19.
 */
public class WebListView extends WebView {

    public WebListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent p_event) {
        return true;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent p_event) {
//        if (p_event.getAction() == MotionEvent.ACTION_MOVE && getParent() != null)
//        {
//            getParent().requestDisallowInterceptTouchEvent(true);
//        }
//
//        return super.onTouchEvent(p_event);
//    }
}
