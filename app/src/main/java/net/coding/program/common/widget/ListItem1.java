package net.coding.program.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.coding.program.R;

/**
 * Created by chenchao on 15/5/28.
 */
public class ListItem1 extends FrameLayout {

    private View mIcon;
    private TextView mText;

    public ListItem1(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.list_item_1, this);
        mIcon = findViewById(R.id.icon);
        mText = (TextView) findViewById(R.id.title);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ListItem1);
        String title = array.getString(R.styleable.ListItem1_itemTitle);
        int icon = array.getInt(R.styleable.ListItem1_itemIcon, R.drawable.user_home_project);
        array.recycle();

        if (title == null) title = "qq";
        mText.setText(title);
        mIcon.setBackgroundResource(icon);
    }
}
