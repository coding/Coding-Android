package net.coding.program.project;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import net.coding.program.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;

@EActivity(R.layout.activity_my_project)
//@OptionsMenu(R.menu.menu_my_project)
public class MyProjectActivity extends ActionBarActivity {

    @AfterViews
    protected final void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Fragment fragment = ProjectFragment_.builder().build();
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
