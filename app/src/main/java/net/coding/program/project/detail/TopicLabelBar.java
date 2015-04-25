package net.coding.program.project.detail;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.model.TopicLabelObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.List;

@EViewGroup(R.layout.project_topic_label_bar)
public class TopicLabelBar extends RelativeLayout {
    @ViewById
    View emptyView, action_edit;
    @ViewById
    FlowLayout flowLayout;
    private List<TopicLabelObject> mData = new ArrayList<>();

    public TopicLabelBar(Context context) {
        super(context);
    }

    public TopicLabelBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TopicLabelBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TopicLabelBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public static interface RemoveListener {
        void onRemove(TopicLabelObject label);
    }

    public static interface EditListener {
        void onEdit();
    }

    public void bind(List<TopicLabelObject> labels, final RemoveListener removeListener, final EditListener editListener) {
        final OnClickListener labelClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == null || !(v.getTag() instanceof TopicLabelObject)) return;
                TopicLabelObject data = (TopicLabelObject) v.getTag();
                mData.remove(data);
                updateEmptyView();
                flowLayout.removeView(v);
                removeListener.onRemove(data);
            }
        };
        action_edit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editListener.onEdit();
            }
        });
        action_edit.setVisibility(editListener == null ? View.GONE : View.VISIBLE);

        mData.clear();
        flowLayout.removeAllViews();
        if (labels != null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            for (TopicLabelObject item : labels) {
                mData.add(item);
                TextView view = (TextView) inflater.inflate(R.layout.project_topic_label_bar_item, flowLayout, false);
                view.setText(item.name);
                if (removeListener != null) {
                    view.setTag(item);
                    view.setOnClickListener(labelClickListener);
                } else {
                    view.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
                flowLayout.addView(view);
            }
        }
        updateEmptyView();
    }

    private void updateEmptyView() {
        emptyView.setVisibility(mData.size() == 0 ? VISIBLE : GONE);
    }
}
