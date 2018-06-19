package net.coding.program.setting;

import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.network.util.Login;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.login.phone.Close2FAActivity_;
import net.coding.program.login.phone.PhoneSetPasswordActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

@EActivity(R.layout.activity_account_setting)
public class AccountSetting extends BackActivity {

    private static final int RESULT_PHONE_SETTING = 1;
    private static final int RESULT_CLOZE_2FA = 2;

    @ViewById
    TextView email, suffix, phone;

    @ViewById
    View phoneSetting, close2FA, close2FALine;

    @AfterViews
    final void initAccountSetting() {
        if (GlobalData.isPrivateEnterprise()) {
            close2FA.setVisibility(View.GONE);
            close2FALine.setVisibility(View.GONE);
        }

        UserObject userObject = GlobalData.sUserObject;
        email.setText(userObject.email);
        suffix.setText(userObject.global_key);
        updatePhoneDisplay();

        String host = Global.HOST_API + "/current_user";
        MyAsyncHttpClient.get(this, host, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                if (isFinishing()) {
                    return;
                }

                UserObject user = new UserObject(response.optJSONObject("data"));
                AccountInfo.saveAccount(AccountSetting.this, user);
                GlobalData.sUserObject = user;
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
        if (GlobalData.isPrivateEnterprise()) {
            showButtomToast("App 暂不支持设置手机号码");
            return;
        }
        ValidePhoneActivity_.intent(this).startForResult(RESULT_PHONE_SETTING);
    }

    @Click
    void forgetPassword() {
        if (GlobalData.isPrivateEnterprise()) {
            CodingCompat.instance().launchEnterprisePrivateEmailSetPasswordActivity(this);
            return;
        }

        if (GlobalData.sUserObject.isPhoneValidation()) {
            PhoneSetPasswordActivity_.intent(this)
                    .account(GlobalData.sUserObject.phone)
                    .start();
        } else if (GlobalData.sUserObject.isEmailValidation()) {
            PhoneSetPasswordActivity_.intent(this)
                    .account(GlobalData.sUserObject.email)
                    .start();
        } else {
            new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                    .setMessage("绑定手机或邮箱后才能找回密码")
                    .setPositiveButton("绑定手机", (dialog, which) -> {
                        phoneSetting();
                    })
                    .setNegativeButton("绑定邮箱", ((dialog, which) -> {
                        emailLayout();
                    }))
                    .show();
            return;
        }
    }

    @OnActivityResult(RESULT_PHONE_SETTING)
    void onResultPhone() {
        updatePhoneDisplay();
    }

    private void updatePhoneDisplay() {
        String phoneString = GlobalData.sUserObject.phone;
        if (!phoneString.isEmpty()) {
            phone.setText(phoneString);
//            phone.setCompoundDrawables(null, null, null, null);
        } else {
            phone.setText("未绑定");
        }

        if (!GlobalData.sUserObject.getTwofaEnabled()) {
            close2FA.setVisibility(View.GONE);
            close2FALine.setVisibility(View.GONE);
        }

        String emailString = GlobalData.sUserObject.email;
        if (!emailString.isEmpty()) {
            boolean emailValid = GlobalData.sUserObject.isEmailValidation();
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
        // 企业版不能修改邮箱
        if (GlobalData.isEnterprise()) {
            return;
        }

        String emailString = GlobalData.sUserObject.email;
        boolean emailValid = GlobalData.sUserObject.isEmailValidation();
        if (!emailString.isEmpty() && !emailValid) {
            new AlertDialog.Builder(this)
                    .setItems(new String[]{"修改邮箱", "重发激活邮件"}, ((dialog, which) -> {
                        if (which == 1) {
                            popResendEmailDialog();
                        } else {
                            ModifyEmailActivity_.intent(this).start();
                        }
                    }))
                    .show();
        } else {
            ModifyEmailActivity_.intent(this).start();
        }
    }

    private void popResendEmailDialog() {
        new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setTitle("激活邮件")
                .setMessage(R.string.alert_activity_email2)
                .setPositiveButton("重发激活邮件", (dialog, which) -> {
                    Login.resendActivityEmail(AccountSetting.this);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Click
    void close2FA() {
        Close2FAActivity_.intent(this).startForResult(RESULT_CLOZE_2FA);
    }


    @OnActivityResult(RESULT_CLOZE_2FA)
    void onResultClose2FA() {
        updatePhoneDisplay();
    }

}
