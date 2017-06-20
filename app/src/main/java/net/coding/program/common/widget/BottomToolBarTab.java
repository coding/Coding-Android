package net.coding.program.common.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.coding.program.R;

/**
 * Created by chenchao on 2017/6/20.
 */

public class BottomToolBarTab extends FrameLayout {

    private TextView textView;

    public BottomToolBarTab(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.widget_bottom_item, this);
        textView = (TextView) findViewById(R.id.itemText);
    }

    public void setData(int id, int drawableId, String title) {
        textView.setId(id);
        textView.setText(title);
        textView.setCompoundDrawablesWithIntrinsicBounds(drawableId, 0, 0, 0);
    }

    public TextView getTextView() {
        return textView;
    }
}
