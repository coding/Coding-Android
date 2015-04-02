package net.coding.program.project.init.create;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import net.coding.program.R;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;

/**
 * Created by jack wang on 2015/3/31.
 */
@EActivity(R.layout.init_activity_common)
public class ProjectCreateActivity extends ActionBarActivity{

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
