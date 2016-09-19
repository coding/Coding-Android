package net.coding.program.setting;

import android.view.Menu;
import android.view.MenuInflater;

import net.coding.program.R;
import net.coding.program.WebActivity;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;


@EActivity(R.layout.activity_web)
public class HelpActivity extends WebActivity {

    @OptionsItem
    void actionFeedback() {
        actionbarTitle.setText("帮助与反馈");
        FeedbackActivity_.intent(this).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.help, menu);
        return true;
    }
}
