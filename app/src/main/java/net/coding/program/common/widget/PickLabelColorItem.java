package net.coding.program.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;

/**
 * 选择颜色列表的 item.
 */
public class PickLabelColorItem extends FrameLayout {

    private ImageView imageColor;
    private TextView title;
    private View picked;

    public PickLabelColorItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    private void init(AttributeSet attrs, int defStyle) {
        inflate(getContext(), R.layout.list_item_pick_label_color, this);
        imageColor = (ImageView) findViewById(R.id.icon);
        title = (TextView) findViewById(R.id.title);
        picked = findViewById(R.id.picked);

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.PickLabelColorItem, defStyle, 0);

        String text = a.getString(R.styleable.PickLabelColorItem_labelColorString);
        if (text == null) text = "";

        int color = a.getColor(R.styleable.PickLabelColorItem_labelColor, 0xFF000000);
        boolean isPicked = a.getBoolean(R.styleable.PickLabelColorItem_labelColorPicked, false);

        boolean showTopLine = a.getBoolean(R.styleable.PickLabelColorItem_labelColorTopLine, true);
        if (!showTopLine) {
            findViewById(R.id.topLine).setVisibility(GONE);
        }

        a.recycle();

        imageColor.setImageDrawable(new ColorDrawable(color));
        title.setText(text);
        picked.setVisibility(isPicked ? VISIBLE : INVISIBLE);
    }


    public void setContent(int color, String titleString) {
        imageColor.setImageDrawable(new ColorDrawable(color));
        title.setText(titleString);
    }

    public int getColor() {
        return ((ColorDrawable) imageColor.getDrawable()).getColor();
    }

    public void setPicked() {
        picked.setVisibility(VISIBLE);
    }
}
