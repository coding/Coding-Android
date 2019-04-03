package net.coding.program.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import net.coding.program.R;


/**
 * Created by chenchao on 16/4/11.
 * loadingView 控件, 某些界面取代 dialog loading
 */
public class LoadingView extends FrameLayout {

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.loading_view, this);

        initAnimator();
    }

    private void initAnimator() {
    }
}
