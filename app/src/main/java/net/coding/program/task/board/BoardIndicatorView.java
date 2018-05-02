package net.coding.program.task.board;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import net.coding.program.R;

/**
 * Created by chenchao on 15/6/26.
 * 用点表示当前 ViewPager 是第几页
 */
public class BoardIndicatorView extends FrameLayout {

    private LinearLayout layout;
    private int mSelect = -1;

    public BoardIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.indicator_view, this);
        layout = (LinearLayout) findViewById(R.id.layout);
    }

    public void setCount(int count, int pickPos) {
        if (count < 0 || count <= pickPos) {
            return;
        }

        if (layout.getChildCount() != count) {
            layout.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(layout.getContext());
            for (int i = 0; i < count; ++i) {
                View point = inflater.inflate(R.layout.board_point, layout, false);
                if (i == count - 1) {
                    point.setBackgroundResource(R.drawable.point_add_normal);
                } else {
                    point.setBackgroundResource(R.drawable.point_normal);
                }

                layout.addView(point);
            }
        }
        setSelect(pickPos);
    }

    public void setSelect(int pos) {
        int count = layout.getChildCount();
        if (pos >= count) {
            return;
        }

        if (0 <= mSelect && mSelect < count) {
            if (mSelect == count - 1) {
                layout.getChildAt(mSelect).setBackgroundResource(R.drawable.point_add_normal);
            } else {
                layout.getChildAt(mSelect).setBackgroundResource(R.drawable.point_normal);
            }
        }

        if (pos == count - 1) {
            layout.getChildAt(pos).setBackgroundResource(R.drawable.point_add_pick);
        } else {
            layout.getChildAt(pos).setBackgroundResource(R.drawable.point_pick);
        }

        mSelect = pos;
    }

}
