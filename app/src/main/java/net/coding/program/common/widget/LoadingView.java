package net.coding.program.common.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

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

        ImageView loadingView = (ImageView) findViewById(R.id.loadingCircle);
        ObjectAnimator animator = ObjectAnimator.ofFloat(loadingView, "rotation", 0f, 360f);
        animator.setDuration(2000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setRepeatCount(2000);
        animator.start();

        ImageView loadingIcon = (ImageView) findViewById(R.id.loadingIcon);
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(loadingIcon, "alpha", 1f, 0f);
        animator1.setDuration(2000);
        animator1.setRepeatMode(ValueAnimator.REVERSE);
        animator1.setInterpolator(new DecelerateInterpolator());
        animator1.setRepeatCount(2000);
        animator1.start();
    }
}
