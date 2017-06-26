package net.coding.program.common.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import net.coding.program.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

/*
 * 使用 common_actionbar 的 activity
 */

@EActivity(R.layout.activity_base_annotation)
public class CodingToolbarBackActivity extends BaseActivity {

    private Toolbar toolbar;
    private TextView toolbarTitle;

    protected void onClickTollbarTitle() {

    }

    @AfterViews
    protected final void annotationDispayHomeAsUp() {
        toolbar = (Toolbar) findViewById(R.id.codingToolbar);
        if (toolbar == null) {
            throw new RuntimeException("not include codingToolbar");
        }

        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbarTitle);
        toolbarTitle.setOnClickListener(v -> onClickTollbarTitle());

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

}
