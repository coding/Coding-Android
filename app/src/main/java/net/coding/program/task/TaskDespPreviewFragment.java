package net.coding.program.task;


import android.view.View;

import net.coding.program.R;
import net.coding.program.project.detail.TopicPreviewFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_topic_preview)
public class TaskDespPreviewFragment extends TopicPreviewFragment {

    @ViewById(R.id.topDivideLine)
    protected View topDivideLine;

    @AfterViews
    protected void initTaskDespPreviewFragment() {
        title.setVisibility(View.GONE);
        labelBar.setVisibility(View.GONE);
        if (topDivideLine != null) {
            topDivideLine.setVisibility(View.GONE);
        }
    }
}
