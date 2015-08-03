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
 * 用点表示当前 ViewPager 是第几页
 */
public class IndicatorView extends FrameLayout {

    private int pointResourceId;

    private LinearLayout layout;
    private int mSelect = -1;

    public IndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.indicator_view, this);
        layout = (LinearLayout) findViewById(R.id.layout);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IndicatorView);
        int count = typedArray.getInt(R.styleable.IndicatorView_pointCount, 0);
        pointResourceId = typedArray.getResourceId(R.styleable.IndicatorView_pointView, R.layout.guide_point);

        for (int i = 0; i < count; ++i) {
            View.inflate(context, pointResourceId, layout);
        }

        if (count > 0) {
            layout.getChildAt(0).setSelected(true);
        }
    }

    public void setCount(int count, int pickPos) {
        if (count < 0 || count <= pickPos) {
            return;
        }

        if (layout.getChildCount() != count) {
            layout.removeAllViews();
            for (int i = 0; i < count; ++i) {
                View.inflate(getContext(), pointResourceId, layout);
            }
        }
        setSelect(pickPos);
    }

    public void setSelect(int pos) {
        if (pos == mSelect) {
            return;
        }

        int count = layout.getChildCount();
        if (pos >= count) {
            return;
        }

        if (0 <= mSelect && mSelect < count) {
            layout.getChildAt(mSelect).setSelected(false);
        }

        layout.getChildAt(pos).setSelected(true);

        mSelect = pos;
    }

}
