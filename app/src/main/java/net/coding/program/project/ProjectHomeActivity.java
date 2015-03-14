package net.coding.program.project;

import android.support.v4.app.Fragment;
import android.widget.FrameLayout;

import net.coding.program.BaseActivity;
import net.coding.program.R;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;

@EActivity(R.layout.activity_project_home)
//@OptionsMenu(R.menu.menu_project_home)
public class ProjectHomeActivity extends BaseActivity {

    @Extra
    ProjectObject mProjectObject;

    @ViewById
    FrameLayout container;

    @StringArrayRes
    String[] dynamic_type_params;

    @AfterViews
    protected void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Fragment fragment;
        if (mProjectObject.isPublic()) {
            fragment = PublicProjectHomeFragment_.builder()
                    .mProjectObject(mProjectObject)
                    .build();
        } else {
            fragment = PrivateProjectHomeFragment_.builder()
                    .mProjectObject(mProjectObject)
                    .mType(dynamic_type_params[0])
                    .build();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }

    @OptionsItem(android.R.id.home)
    final protected void clickBack() {
        finish();
    }
}
