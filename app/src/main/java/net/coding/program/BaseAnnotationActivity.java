package net.coding.program;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;


@EActivity(R.layout.activity_base_annotation)
public class BaseAnnotationActivity extends BaseActivity {

    @AfterViews
    protected void annotationDispayHomeAsUp() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OptionsItem(android.R.id.home)
    protected void annotaionClose() {
        onBackPressed();
    }
}
