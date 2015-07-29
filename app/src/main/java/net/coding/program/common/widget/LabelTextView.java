package net.coding.program.common.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.TextView;

import net.coding.program.R;

/**
 * Created by chenchao on 15/7/21.
 * 标签控件
 */
public class LabelTextView extends TextView {

    public LabelTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setText(String text, int color) {
        setBackgroundResource(R.drawable.round_rect_shape_green);
        ((GradientDrawable) getBackground()).setColor(color);
        setText(text);

        double value = Color.red(color) * 0.299 + Color.green(color) * 0.587 + Color.blue(color) * 0.114;
        if (value < 186) {
            setTextColor(0xffffffff);
        } else {
            setTextColor(0xff222222);
        }
    }
}
