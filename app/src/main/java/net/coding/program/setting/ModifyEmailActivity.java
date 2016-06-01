package net.coding.program.setting;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.SimpleSHA1;
import net.coding.program.common.WeakRefHander;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.ViewStyleUtil;
import net.coding.program.common.widget.LoginEditText;
import net.coding.program.login.auth.AuthInfo;
import net.coding.program.login.auth.TotpClock;
import net.coding.program.model.AccountInfo;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

@EActivity(R.layout.activity_modify_email)
public class ModifyEmailActivity extends BackActivity {

    @ViewById
    TextView okButton;

    @ViewById
    LoginEditText emailEdit, captchaEdit, passwordEdit, twoFAEdit;

    Handler handler2FA;

    @AfterViews
    void initModifyEmailActivity() {
        String emailString = MyApp.sUserObject.email;
        if (emailString.isEmpty()) {
            setTitle("绑定邮箱");
        }

        handler2FA = new WeakRefHander(msg -> {
            if (twoFAEdit.getVisibility() == View.VISIBLE) {
                String secret = AccountInfo.loadAuth(this, MyApp.sUserObject.global_key);
                if (secret.isEmpty()) {
                    return true;
                }

                String code2FA = new AuthInfo(secret, new TotpClock(this)).getCode();
                twoFAEdit.setText(code2FA);
            }

            return true;
        }, 100);

        final String url = Global.HOST_API + "/user/2fa/method";
        MyAsyncHttpClient.get(this, url, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                String type = response.optString("data");
                if (type.equals("password")) {
                    passwordEdit.setVisibility(View.VISIBLE);
                    twoFAEdit.setVisibility(View.GONE);
                    ViewStyleUtil.editTextBindButton(okButton, emailEdit, captchaEdit, passwordEdit);
                } else {
                    passwordEdit.setVisibility(View.GONE);
                    twoFAEdit.setVisibility(View.VISIBLE);
                    ViewStyleUtil.editTextBindButton(okButton, emailEdit, captchaEdit, twoFAEdit);
                    handler2FA.sendEmptyMessage(0);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (handler2FA != null) {
            handler2FA.removeMessages(0);
        }
        super.onDestroy();
    }

    @Click
    void okButton() {
        final String url = Global.HOST_API + "/account/email/change/send";
        RequestParams param = new RequestParams();
        String email = emailEdit.getTextString();
        String captch = captchaEdit.getTextString();
        String twoFA;
        if (passwordEdit.getVisibility() == View.VISIBLE) {
            twoFA = SimpleSHA1.sha1(passwordEdit.getTextString());
        } else {
            twoFA = twoFAEdit.getTextString();
        }
        param.put("email", email);
        param.put("j_captcha", captch);
        param.put("two_factor_code", twoFA);

        MyAsyncHttpClient.post(this, url, param, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                showProgressBar(false);
                showMiddleToast("发送验证邮件成功");
                finish();
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
                showProgressBar(false);
                captchaEdit.requestCaptcha();
                captchaEdit.setText("");
            }
        });

        showProgressBar(true);
    }
}
