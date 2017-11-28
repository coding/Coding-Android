package net.coding.program.common.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import net.coding.program.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;

@EActivity
public class BackActivity extends BaseActivity {

    @AfterViews
    protected final void annotationDispayHomeAsUp() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @OptionsItem(android.R.id.home)
    protected final void annotaionClose() {
        onBackPressed();
    }

    protected final void useToolbar() {
        View v = findViewById(R.id.toolbar);
        if (v instanceof Toolbar) {
            setSupportActionBar((Toolbar) v);

            ActionBar supportActionBar = getSupportActionBar();
            if (supportActionBar != null) {
                supportActionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }
}
