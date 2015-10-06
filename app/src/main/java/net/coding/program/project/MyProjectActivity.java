package net.coding.program.project;

import android.support.v4.app.Fragment;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_my_project)
//@OptionsMenu(R.menu.menu_my_project)
public class MyProjectActivity extends BackActivity {

    @AfterViews
    protected final void init() {
        Fragment fragment = ProjectFragment_.builder().build();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
