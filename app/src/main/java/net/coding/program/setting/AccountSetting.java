package net.coding.program.setting;

import android.widget.TextView;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_account_setting)
public class AccountSetting extends BackActivity {

    private static final int RESULT_PHONE_SETTING = 1;
    @ViewById
    TextView email, suffix, phone;

    @AfterViews
    final void initAccountSetting() {
        UserObject userObject = MyApp.sUserObject;
        email.setText(userObject.email);
        suffix.setText(userObject.global_key);
        updatePhoneDisplay();
    }

    @Click
    void phoneSetting() {
        ValidePhoneActivity_.intent(this).startForResult(RESULT_PHONE_SETTING);
    }

    @OnActivityResult(RESULT_PHONE_SETTING)
    void onResultPhone() {
        updatePhoneDisplay();
    }

    private void updatePhoneDisplay() {
        String phoneString = MyApp.sUserObject.phone;
        if (phoneString.isEmpty()) {
            phone.setText("未认证");
            phone.setTextColor(0xFFF34A4A);
        } else {
            phone.setText(phoneString);
            phone.setTextColor(0xFF666666);

        }
    }

    @Click
    void passwordSetting() {
        SetPasswordActivity_.intent(this).start();
    }
}
