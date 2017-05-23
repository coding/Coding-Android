package net.coding.program.project.init.setting;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * Created by jack wang on 2015/3/31.
 *
 */
@EActivity(R.layout.init_activity_common)
public class ProjectSetActivity extends BackActivity {

    ProjectSetFragment fragment;

    @Extra
    ProjectObject projectObject;

    @AfterViews
    protected final void initProjectSetActivity() {
        fragment = ProjectSetFragment_.builder().mProjectObject(projectObject).build();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (fragment.isBackToRefresh) {
            fragment.backToRefresh();
            return;
        }
        finish();
    }
}
