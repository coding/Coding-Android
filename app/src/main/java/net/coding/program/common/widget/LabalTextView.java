package net.coding.program.common.widget;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.TextView;

import net.coding.program.R;

/**
 * Created by chenchao on 15/7/21.
 */
public class LabalTextView extends TextView {

    public LabalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTextColor(0xffffffff);
        int paddingHeight = getResources().getDimensionPixelSize(R.dimen.labal_padding_height);
        int paddingWidth = getResources().getDimensionPixelSize(R.dimen.labal_padding_width);
        setPadding(paddingWidth, paddingHeight, paddingWidth, paddingHeight);
    }

    public void setText(String text, int color) {
        setBackgroundResource(R.drawable.round_rect_shape_green);
        ((GradientDrawable) getBackground()).setColor(color);
        setText(text);
    }
}
