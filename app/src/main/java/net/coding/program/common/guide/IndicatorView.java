package net.coding.program.common.guide;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import net.coding.program.R;

/**
 * Created by chenchao on 15/6/26.
 */
public class IndicatorView extends FrameLayout {

    private LinearLayout layout;
    private int mSelect = 0;

    public IndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.indicator_view, this);
        layout = (LinearLayout) findViewById(R.id.layout);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IndicatorView);
        int count = typedArray.getInt(R.styleable.IndicatorView_pointCount, 0);

        for (int i = 0; i < count; ++i) {
            View.inflate(context, R.layout.guide_point, layout);
        }

        if (count > 0) {
            layout.getChildAt(0).setBackgroundResource(R.drawable.guide_point_black);
        }
    }

    public void setSelect(int pos) {
        if (pos == mSelect) {
            return;
        }

        int count = layout.getChildCount();
        if (pos >= count) {
            return;
        }

        if (mSelect < count) {
            layout.getChildAt(mSelect).setBackgroundResource(R.drawable.guide_point_white);
        }
        layout.getChildAt(pos).setBackgroundResource(R.drawable.guide_point_black);

        mSelect = pos;
    }

}
