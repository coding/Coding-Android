package net.coding.program.common.widget.bottombar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;

/**
 * Created by chenchao on 16/9/26.
 */

public class BottomBarTab extends FrameLayout {

    TextView text;
    ImageView icon;
    boolean isPicked;

    public BottomBarTab(Context context, AttributeSet attrs) {
        super(context, attrs);

        View.inflate(context, R.layout.bottom_bar_item, this);
    }


}
