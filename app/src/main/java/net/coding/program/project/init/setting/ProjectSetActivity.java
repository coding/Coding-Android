package net.coding.program.project.init.setting;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import net.coding.program.R;
import net.coding.program.model.ProjectObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;

/**
 * Created by jack wang on 2015/3/31.
 */
@EActivity(R.layout.init_activity_common)
public class ProjectSetActivity extends ActionBarActivity{

    ProjectSetFragment fragment;

    @AfterViews
    protected final void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fragment = ProjectSetFragment_.builder().build();
        ProjectObject projectObject= (ProjectObject) getIntent().getSerializableExtra("projectObject");
        Bundle bundle=new Bundle();
        bundle.putSerializable("projectObject",projectObject);
        fragment.setArguments(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @OptionsItem(android.R.id.home)
    protected final void back() {
        if (fragment.isBackToRefresh){
            fragment.backToRefresh();
            return;
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if (fragment.isBackToRefresh){
            fragment.backToRefresh();
            return;
        }
        finish();
    }
}
