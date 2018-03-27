package net.coding.program.common.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import net.coding.program.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

/**
 * Created by chenchao on 2017/6/28.
 */

@EActivity
public class ToolbarBackActivity extends BackActivity {

    private Toolbar toolbar;
    private TextView toolbarTitle;

    @AfterViews
    protected void initToolbarBackActivity() {
        toolbar = (Toolbar) findViewById(R.id.codingToolbar);
        if (toolbar == null) {
            throw new RuntimeException("not include codingToolbar");
        }

        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbarTitle);

        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    @Override
    public void setActionBarTitle(String title) {
        toolbarTitle.setText(title);
    }

    protected final Toolbar getToolbar() {
        return toolbar;
    }
}
