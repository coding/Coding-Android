package net.coding.program.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import net.coding.program.R;
import net.coding.program.model.TopicLabelObject;

import org.apmem.tools.layouts.FlowLayout;

import java.util.List;

/**
 * Created by chenchao on 15/7/23.
 * 用于列表控件显示标签
 */
public class FlowLabelLayout extends FlowLayout {

    public FlowLabelLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setLabels(List<TopicLabelObject> list) {
        if (list.isEmpty()) {
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);
        removeAllViews();
        for (TopicLabelObject item : list) {
            LabelTextView label = (LabelTextView) LayoutInflater.from(getContext()).inflate(R.layout.project_topic_list_item_label, this, false);
            addView(label);
            label.setText(item.name, item.getColor());
        }
    }
}
