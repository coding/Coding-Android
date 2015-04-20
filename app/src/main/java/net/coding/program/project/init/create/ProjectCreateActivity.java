package net.coding.program.project.init.create;

import android.support.v7.app.ActionBarActivity;

import net.coding.program.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;

/**
 * Created by jack wang on 2015/3/31.
 */
@EActivity(R.layout.init_activity_common)
public class ProjectCreateActivity extends ActionBarActivity {

    ProjectCreateFragment fragment;

    @AfterViews
    protected final void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fragment = ProjectCreateFragment_.builder().build();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @OptionsItem(android.R.id.home)
    protected final void back() {
        finish();
    }

}
