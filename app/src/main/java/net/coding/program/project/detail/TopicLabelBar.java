package net.coding.program.project.detail;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import net.coding.program.R;
import net.coding.program.common.widget.LabelTextView;
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
    View emptyView, labelView, barView, reservedView;
    @ViewById
    FlowLayout flowLayout;

    private List<TopicLabelObject> mData = new ArrayList<>();
    private Controller controller;
    private OnClickListener onClickLabel = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v != null && (v.getTag() instanceof TopicLabelObject)) {
                TopicLabelObject data = (TopicLabelObject) v.getTag();
                if (controller != null) controller.onRemoveLabel(TopicLabelBar.this, data.id);
            }
        }
    };
    private View buttonAddLabal;

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

        if (flowLayout.getChildCount() == 1 &&
                flowLayout.getChildAt(0) == buttonAddLabal) {
            flowLayout.removeAllViews();
        }
    }

    @Click
    void emptyView() {
        if (controller != null) controller.onEditLabels(TopicLabelBar.this);
    }

    public void bind(List<TopicLabelObject> labels, final Controller controller) {
        this.controller = controller;
        if (!controller.canShowLabels()) {
            barView.setVisibility(GONE);
            reservedView.setVisibility(VISIBLE);
            return;
        }

        mData.clear();

        LinkedList<LabelTextView> cachedViews = new LinkedList<>();
        for (int i = 0, n = flowLayout.getChildCount(); i < n; i++) {
            View view = flowLayout.getChildAt(i);
            if (view instanceof LabelTextView)
                cachedViews.add((LabelTextView) view);
        }

        flowLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (buttonAddLabal == null) {
            buttonAddLabal = inflater.inflate(R.layout.labal_add_button, flowLayout, false);
            buttonAddLabal.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    emptyView();
                }
            });
        }

        for (TopicLabelObject item : labels) {
            mData.add(item);
            LabelTextView view = cachedViews.poll();
            if (view == null) {
                view = (LabelTextView) inflater.inflate(R.layout.project_topic_label_bar_item, flowLayout, false);
            }
            view.setText(item.name, item.getColor());
            view.setTag(item);
            if (controller.canEditLabels()) {
                view.setOnClickListener(onClickLabel);
            } else {
                view.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }
            flowLayout.addView(view);
        }

        if (flowLayout.getChildCount() > 0 && controller.canEditLabels()) {
            flowLayout.addView(buttonAddLabal);
        }
        updateEmptyView();
        emptyView.setVisibility(controller.canEditLabels() && labels.isEmpty() ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateEmptyView() {
        emptyView.setVisibility(mData.isEmpty() ? VISIBLE : GONE);
        labelView.setVisibility(mData.isEmpty() ? GONE : VISIBLE);
    }

    public interface Controller {
        boolean canShowLabels();

        boolean canEditLabels();

        void onEditLabels(TopicLabelBar view);

        void onRemoveLabel(TopicLabelBar view, int labelId);
    }
}
