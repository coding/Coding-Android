package net.coding.program.login.phone;

import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.ViewStyleUtil;
import net.coding.program.common.widget.LoginEditText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

@EActivity(R.layout.activity_email_set_password)
public class EmailSetPasswordActivity extends BackActivity {

    @Extra
    PhoneSetPasswordActivity.Type type;

    @Extra
    String account = "";

    @ViewById
    LoginEditText emailEdit, captchaEdit;

    @ViewById
    TextView loginButton;

    @AfterViews
    void initEmailSetPasswordActivity() {
        setTitle(type.getInputAccountTitle());
        emailEdit.setText(account);
        captchaEdit.requestFocus();

        ViewStyleUtil.editTextBindButton(loginButton, emailEdit, captchaEdit);

        if (type == PhoneSetPasswordActivity.Type.activate) {
            loginButton.setText("重发激活邮件");
        } else {
            loginButton.setText("发送重置邮件");
        }
    }

    @Click
    void loginButton() {
        String emailString = emailEdit.getTextString();
        String captchaString = captchaEdit.getTextString();
        String format = type.getResetPasswordEmailUrl();
        String url = String.format(format, emailString, captchaString);
        MyAsyncHttpClient.get(this, url, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                setResult(RESULT_OK);
                showMiddleToast("邮件已发送");

                if (type == PhoneSetPasswordActivity.Type.activate) {
                    showMiddleToastLong("激活邮件已经发送，请尽快去邮箱查看");
                } else {
                    showMiddleToastLong("重置密码邮件已经发送，请尽快去邮箱查看");
                }

                finish();
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
                captchaEdit.requestCaptcha();
                captchaEdit.requestFocus();
            }

            @Override
            public void onFinish() {
                super.onFinish();
                showProgressBar(false, "");
            }
        });

        showProgressBar(true, "");
    }

}
