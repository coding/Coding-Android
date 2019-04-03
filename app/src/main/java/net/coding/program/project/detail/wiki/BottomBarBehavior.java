package net.coding.program.project.detail.wiki;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by chenchao on 2017/4/12.
 * toolbar 隐藏的时候也隐藏底栏
 */
public class BottomBarBehavior extends CoordinatorLayout.Behavior<View> {

    public BottomBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        float y = Math.abs(dependency.getTop());
        child.setTranslationY(y);
        return true;
    }
}
