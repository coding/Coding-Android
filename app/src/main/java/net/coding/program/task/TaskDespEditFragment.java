package net.coding.program.task;


import android.view.View;

import net.coding.program.R;
import net.coding.program.project.detail.TopicEditFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_topic_edit)
public class TaskDespEditFragment extends TopicEditFragment {

    @ViewById(R.id.topDivideLine)
    protected View divideLine;

    @AfterViews
    protected void initTaskDespEditFragment() {
        title.setVisibility(View.GONE);
        labelBar.setVisibility(View.GONE);
        divideLine.setVisibility(View.GONE);
    }
}
