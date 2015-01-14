package net.coding.program.setting;

import android.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import net.coding.program.BaseActivity;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_account_setting)
public class AccountSetting extends BaseActivity {

    @ViewById
    TextView email;

    @ViewById
    TextView suffix;

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);

        UserObject userObject = MyApp.sUserObject;
        email.setText(userObject.email);
        suffix.setText(userObject.global_key);
    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    @Click
    void passwordSetting() {
        SetPasswordActivity_.intent(this).start();
    }
}
