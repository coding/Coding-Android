package net.coding.program.setting;

import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import net.coding.program.EntranceActivity;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.network.util.Login;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

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

        MyAsyncHttpClient.get(this, EntranceActivity.HOST_CURRENT, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                if (isFinishing()) {
                    return;
                }

                UserObject user = new UserObject(response.optJSONObject("data"));
                AccountInfo.saveAccount(AccountSetting.this, user);
                MyApp.sUserObject = user;
                AccountInfo.saveReloginInfo(AccountSetting.this, user);
                updatePhoneDisplay();
            }

            @Override
            public void onMyFailure(JSONObject response) {
            }
        });
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
        if (!phoneString.isEmpty()) {
            phone.setText(phoneString);
//            phone.setCompoundDrawables(null, null, null, null);
        } else {
            phone.setText("未绑定");
        }

        String emailString = MyApp.sUserObject.email;
        if (!emailString.isEmpty()) {
            boolean emailValid = MyApp.sUserObject.isEmailValidation();
            if (emailValid) {
                email.setText(emailString);
            } else {
                emailString += " " + "未验证";
            }
        } else {
            emailString += " " + "未绑定";
        }
        email.setText(emailString);
    }

    @Click
    void passwordSetting() {
        SetPasswordActivity_.intent(this).start();
    }

    @Click
    void emailLayout() {
        String emailString = MyApp.sUserObject.email;
        boolean emailValid = MyApp.sUserObject.isEmailValidation();
        if (!emailString.isEmpty() && !emailValid) {
            new AlertDialog.Builder(this)
                    .setTitle("激活邮件")
                    .setMessage(R.string.alert_activity_email2)
                    .setPositiveButton("重发激活邮件", (dialog, which) -> {
                        Login.resendActivityEmail(AccountSetting.this);
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            ModifyEmailActivity_.intent(this).start();
        }
    }
}
