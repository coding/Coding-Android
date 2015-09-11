package net.coding.program.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;

/**
 * Created by chenchao on 15/9/10.
 */
public class IconTextView extends FrameLayout {

    public IconTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        View.inflate(context, R.layout.icon_text_view, this);

        ImageView imageView = (ImageView) findViewById(R.id.icon);
        TextView textView = (TextView) findViewById(R.id.text);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.IconTextView);
        String text = array.getString(R.styleable.IconTextView_iconTextTitle);
        int icon = array.getResourceId(R.styleable.IconTextView_iconTextIcon, R.drawable.icon_share_copy_link);

        if (text == null) {
            text = "";
        }
        textView.setText(text);
        imageView.setImageResource(icon);
    }
}
