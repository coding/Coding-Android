package net.coding.program.common.widget;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

/**
 * Created by chenchao on 16/8/23.
 * 为了兼容 5.0 以下的 Android 版本, 统一使用内边距显示卡片阴影,
 * 由于内边距的存在, 导致卡片与其它的控件在视觉上是没有对齐的, 这个控件通过
 * 设置负的 merge 来解决对齐的问题
 * 注意: 父控件要设置
 * android:clipChildren="false"
 * android:clipToPadding="false"
 * https://developer.android.com/reference/android/support/v7/widget/CardView.html
 */
public class AlignCardView extends CardView {

    public boolean isRejust = false;

    public AlignCardView(Context context) {
        super(context);

    }

    public AlignCardView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public AlignCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        init();
    }

    private void init() {
        if (isRejust) {
            return;
        }

        isRejust = true;

        float ele = getMaxCardElevation();
        float radus = getRadius();
        MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();

        int leftExtra= (int) (ele + radus * (1 - 1 / 1.414));
        lp.leftMargin = lp.leftMargin - leftExtra;
        lp.rightMargin = lp.rightMargin - leftExtra;
        int topExtra = (int) (ele * 1.5 + radus * (1 - 1 / 1.414));
        lp.topMargin = lp.topMargin - topExtra;
        lp.bottomMargin = lp.bottomMargin - topExtra;

        setLayoutParams(lp);
    }
}
