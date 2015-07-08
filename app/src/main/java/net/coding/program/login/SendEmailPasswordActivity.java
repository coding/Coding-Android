package net.coding.program.login;

import net.coding.program.R;
import net.coding.program.common.Global;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_base_send_email)
public class SendEmailPasswordActivity extends SendEmailBaseActivity {


    private final String hostResetPassword = Global.HOST_API + "/resetPassword?email=%s&j_captcha=%s";

    @AfterViews
    protected final void initResendEmail() {
        loginButton.setText("发送重置密码邮件");
    }

    @Click
    protected final void loginButton() {
        if (!isEnterSuccess()) {
            return;
        }

        String hostReset = String.format(hostResetPassword, getEmail(), getValify());
        getNetwork(hostReset, hostResetPassword);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(hostResetPassword)) {
            if (code == 0) {
                showMiddleToast("激活邮件已经发送，请尽快去邮箱查收");
            } else {
                downloadValifyPhoto();
                showErrorMsg(code, respanse);
            }
        }
    }

}
