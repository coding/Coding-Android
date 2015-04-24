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
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.apmem.tools.layouts.FlowLayout;

import java.util.List;

@EViewGroup(R.layout.project_topic_label_bar)
public class TopicLabelBar extends RelativeLayout {
    @ViewById
    View emptyView, addButton;
    @ViewById
    FlowLayout flowLayout;

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

    @AfterViews
    void afterViews() {
        bind(null, true);
    }

    @Click
    void addButton() {
        // todo: goto label manager
    }

    private OnClickListener labelClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == null || !(v.getTag() instanceof TopicLabelObject)) return;
            TopicLabelObject data = (TopicLabelObject) v.getTag();
            // todo: remove label async
        }
    };

    public void bind(List<TopicLabelObject> labelList, boolean readonly) {
        addButton.setVisibility(readonly ? View.GONE : View.VISIBLE);
        flowLayout.removeAllViews();
        if (labelList == null || labelList.size() == 0) {
            emptyView.setVisibility(VISIBLE);
            flowLayout.setVisibility(GONE);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (TopicLabelObject item : labelList) {
            TextView view = (TextView) inflater.inflate(R.layout.project_topic_label_bar_item, flowLayout, false);
            view.setText(item.name);
            if (!readonly) {
                view.setTag(item);
                view.setOnClickListener(labelClickListener);
            } else {
                view.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }
            flowLayout.addView(view);
        }
        flowLayout.setVisibility(VISIBLE);
        emptyView.setVisibility(GONE);
    }
}
