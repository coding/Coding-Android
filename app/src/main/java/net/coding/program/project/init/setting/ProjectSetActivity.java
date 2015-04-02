package net.coding.program.project.init.setting;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import net.coding.program.R;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.ProjectFragment;
import net.coding.program.project.init.InitProUtils;

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
            InitProUtils.intentToMain(this);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if (fragment.isBackToRefresh){
            InitProUtils.intentToMain(this);
        }
        finish();
    }

    public boolean onTouchEvent(MotionEvent event) {

        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(getCurrentFocus()!=null && getCurrentFocus().getWindowToken()!=null){
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return super.onTouchEvent(event);
    }



}
