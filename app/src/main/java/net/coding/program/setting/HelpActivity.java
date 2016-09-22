package net.coding.program.setting;

import android.view.Menu;

import net.coding.program.R;
import net.coding.program.WebActivity;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;


@EActivity(R.layout.activity_help)
public class HelpActivity extends WebActivity {

    @Click
    void toolbarFeedback() {
        FeedbackActivity_.intent(this).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
