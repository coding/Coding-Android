package net.coding.program.setting;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.project.ProjectFragment;
import net.coding.program.project.ProjectFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_my_created)
public class MyCreateProjectListActivity extends BackActivity {

    @AfterViews
    void initMyCreateProjectListActivity() {
        ProjectFragment fragment = ProjectFragment_.builder()
                .type(ProjectFragment.Type.Create)
                .build();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }
}
