package net.coding.program.project.detail;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import net.coding.program.R;
import net.coding.program.common.model.TopicLabelObject;
import net.coding.program.common.widget.LabelTextView;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Neutra on 2015/4/25.
 * 标签管理页的 item
 */
@EViewGroup(R.layout.activity_topic_label_item)
public class TopicLabelItemView extends RelativeLayout implements Checkable {

    @ViewById
    LabelTextView textView;

    @ViewById
    ImageView icon;

    TopicLabelObject data;
    private boolean checked = false;

    public TopicLabelItemView(Context context) {
        super(context);
    }

    public TopicLabelItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TopicLabelItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TopicLabelItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;
        icon.setVisibility(checked ? VISIBLE : INVISIBLE);
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }

    public void bind(TopicLabelObject data) {
        this.data = data;
        textView.setText(data.name, data.getColorValue());
    }
}
