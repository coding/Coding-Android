package net.coding.program.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.flyco.roundview.RoundTextView;

import net.coding.program.R;
import net.coding.program.common.model.TopicLabelObject;

import org.apmem.tools.layouts.FlowLayout;

import java.util.List;

/**
 * Created by chenchao on 15/7/23.
 * 用于列表控件显示标签
 */
public class BoardLabelLayout extends FlowLayout {


    public BoardLabelLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setLabels(final List<TopicLabelObject> list, int width) {
        if (list.isEmpty()) {
            setVisibility(GONE);
            return;
        }

        setLabelViews(list, width);
    }

    private void setLabelViews(List<TopicLabelObject> list, int flowWidth) {
        removeAllViews();
        for (TopicLabelObject item : list) {
            RoundTextView label = (RoundTextView) LayoutInflater.from(getContext()).inflate(R.layout.board_list_item_tag, this, false);
            label.setText(item.name);
            label.getDelegate().setBackgroundColor(item.getColorValue());
            addView(label);
        }
    }
}
