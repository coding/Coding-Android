package net.coding.program.project.init.setting;

import net.coding.program.R;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.project.EventProjectModify;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by jack wang on 2015/3/31.
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
    protected boolean userEventBus() {
        return true;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventProjectModify(EventProjectModify event) {
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
