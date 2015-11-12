package net.coding.program.task;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;


@EActivity(R.layout.activity_all_tasks)
public class AllTasksActivity extends BackActivity {

    @AfterViews
    final void initAllTasksActivity() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new TaskFragment_())
                .commit();
    }

}
