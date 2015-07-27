package net.coding.program.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.TextView;

import net.coding.program.R;

/**
 * Created by chenchao on 15/7/21.
 */
public class LabelTextView extends TextView {

    public LabelTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTextColor(0xffffffff);

        int[] attributes = new int[]{
                android.R.attr.paddingLeft,
                android.R.attr.paddingTop,
                android.R.attr.paddingRight,
                android.R.attr.paddingBottom
        };

        TypedArray arr = context.obtainStyledAttributes(attrs, attributes);
        int leftPadding = arr.getDimensionPixelOffset(0, 0);
        int topPadding = arr.getDimensionPixelOffset(1, 0);
        int rightPadding = arr.getDimensionPixelOffset(2, 0);
        int bottomPadding = arr.getDimensionPixelOffset(3, 0);

        if (leftPadding == 0 &&
                rightPadding == 0 &&
                topPadding == 0 &&
                bottomPadding == 0) {

            leftPadding = getResources().getDimensionPixelSize(R.dimen.labal_padding_height);
            rightPadding = leftPadding;
            topPadding = getResources().getDimensionPixelSize(R.dimen.labal_padding_width);
            bottomPadding = topPadding;
        }

        setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
    }

    public void setText(String text, int color) {
        setBackgroundResource(R.drawable.round_rect_shape_green);
        ((GradientDrawable) getBackground()).setColor(color);
        setText(text);
    }
}
