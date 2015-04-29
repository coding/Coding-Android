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

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.LinkedList;
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

    public static interface Controller {
        boolean canEditLabel();
        void onEditLabels(TopicLabelBar view);
        boolean canRemoveLabel();
        void onRemoveLabel(TopicLabelBar view, int labelId);
    }

    public static interface EditListener {
    }

    public void removeLabel(int id) {
        for (int i = 0, n = flowLayout.getChildCount(); i < n; i++) {
            View view = flowLayout.getChildAt(i);
            if (view == null || !(view.getTag() instanceof TopicLabelObject)) continue;
            TopicLabelObject data = (TopicLabelObject) view.getTag();
            if (data.id == id) {
                mData.remove(data);
                updateEmptyView();
                flowLayout.removeView(view);
                break;
            }
        }
    }

    private OnClickListener onClickLabel = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v != null && (v.getTag() instanceof TopicLabelObject)) {
                TopicLabelObject data = (TopicLabelObject) v.getTag();
                if (controller != null)  controller.onRemoveLabel(TopicLabelBar.this, data.id);
            }
        }
    };

    private Controller controller;

    @Click
    void action_edit() {
        if (controller != null) controller.onEditLabels(TopicLabelBar.this);
    }

    public void bind(List<TopicLabelObject> labels, final Controller controller) {
        this.controller = controller;
        action_edit.setVisibility(controller.canEditLabel() ? View.VISIBLE : View.INVISIBLE);

        mData.clear();
        LinkedList<TextView> cachedViews = new LinkedList<>();
        for (int i = 0, n = flowLayout.getChildCount(); i < n; i++) {
            View view = flowLayout.getChildAt(i);
            if (view instanceof TextView) cachedViews.add((TextView) view);
        }
        flowLayout.removeAllViews();
        if (labels != null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            for (TopicLabelObject item : labels) {
                mData.add(item);
                TextView view = cachedViews.poll();
                if (view == null)
                    view = (TextView) inflater.inflate(R.layout.project_topic_label_bar_item, flowLayout, false);
                view.setText(item.name);
                view.setTag(item);
                if (controller.canRemoveLabel()) {
                    view.setOnClickListener(onClickLabel);
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
