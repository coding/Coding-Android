package net.coding.program.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.model.TopicLabelObject;

import org.apmem.tools.layouts.FlowLayout;

import java.util.List;

/**
 * Created by chenchao on 15/7/23.
 * 用于列表控件显示标签
 */
public class FlowLabelLayout extends FlowLayout {

    TextView textLabel; // 用来计算高度的，不添加子控件
    int itemExtra = 0; // item 除了文字外占用的空间长度，包括 padding，merge

    public FlowLabelLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        textLabel = (LabelTextView) LayoutInflater.from(getContext()).inflate(R.layout.project_topic_list_item_label, this, false);
        itemExtra = getResources().getDimensionPixelSize(R.dimen.label_list_item_merge_right) +
                getResources().getDimensionPixelSize(R.dimen.label_list_item_padding_left) * 2;
    }

    public void setLabels(final List<TopicLabelObject> list, int width) {
        if (list.isEmpty()) {
            setVisibility(GONE);
            return;
        }

        setLabelViews(list, width);
    }

    private void setLabelViews(List<TopicLabelObject> list, int flowWidth) {
        setVisibility(VISIBLE);
        removeAllViews();

        int realWidth = 0;

        float endWidth = textLabel.getPaint().measureText("...");
        for (TopicLabelObject item : list) {
            float itemWidth = textLabel.getPaint().measureText(item.name) + itemExtra;
            if (realWidth + itemWidth + endWidth < flowWidth) {
                realWidth += itemWidth;
                LabelTextView label = (LabelTextView) LayoutInflater.from(getContext()).inflate(R.layout.project_topic_list_item_label, this, false);
                addView(label);
                label.setText(item.name, item.getColor());
            } else {
                View end = LayoutInflater.from(getContext()).inflate(R.layout.project_topic_list_item_label_more, this, false);
                addView(end);
                break;
            }
        }
    }
}
