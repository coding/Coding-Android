package net.coding.program.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.coding.program.R;

/**
 * Created by chenchao on 15/5/28.
 * listview item，带一个 icon 和一行文字
 */
public class ListItem1 extends FrameLayout {

    private View mIcon;
    private TextView mText;
    private View badge;

    public ListItem1(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.list_item_1, this);
        mIcon = findViewById(R.id.icon);
        mText = (TextView) findViewById(R.id.title);
        badge = findViewById(R.id.badge);
        badge.setVisibility(INVISIBLE);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ListItem1);
        String title = array.getString(R.styleable.ListItem1_itemTitle);
        int icon = array.getResourceId(R.styleable.ListItem1_itemIcon, R.drawable.user_home_project);

        boolean center = array.getBoolean(R.styleable.ListItem1_itemCenter, false);
        array.recycle();

        if (title == null) title = "";
        mText.setText(title);
        mIcon.setBackgroundResource(icon);

        if (center) {
            findViewById(R.id.arrow).setVisibility(GONE);

            LinearLayout.LayoutParams layoutParams;
            layoutParams = (LinearLayout.LayoutParams) mText.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.weight = 0;
            ((LinearLayout) findViewById(R.id.clickProject)).setGravity(Gravity.CENTER);
        }
    }

    public void setText(String s) {
        if (s == null) {
            return;
        }

        mText.setText(s);
    }

    public void showBadge(boolean show) {
        badge.setVisibility(show ? VISIBLE : INVISIBLE);
    }
}
