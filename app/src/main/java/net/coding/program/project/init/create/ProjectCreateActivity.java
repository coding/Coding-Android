package net.coding.program.project.init.create;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

/**
 * Created by jack wang on 2015/3/31.
 */
@EActivity(R.layout.init_activity_common)
public class ProjectCreateActivity extends BackActivity {

    ProjectCreateFragment fragment;

    @AfterViews
    protected final void init() {
        fragment = ProjectCreateFragment_.builder().build();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
