package net.coding.program.project.detail;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.coding.program.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Neutra on 2015/4/24.
 */
@EViewGroup(R.layout.dropdown_tab_button)
public class DropdownButton extends RelativeLayout {
    @ViewById
    TextView textView;
    @ViewById
    View bottomLine;


    public DropdownButton(Context context) {
        super(context);
    }

    public DropdownButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DropdownButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DropdownButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setText(CharSequence text) {
        textView.setText(text);
    }

    public void setChecked(boolean checked) {
        Drawable icon;
        if (checked) {
            icon = getResources().getDrawable(R.drawable.ic_dropdown_actived);
            textView.setTextColor(getResources().getColor(R.color.green));
            bottomLine.setVisibility(VISIBLE);
        } else {
            icon = getResources().getDrawable(R.drawable.ic_dropdown_normal);
            textView.setTextColor(getResources().getColor(R.color.font_black_content));
            bottomLine.setVisibility(GONE);
        }
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
    }
}
