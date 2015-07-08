package net.coding.program.login;

import net.coding.program.R;
import net.coding.program.common.Global;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_base_send_email)
public class SendEmailActiveActivity extends SendEmailBaseActivity {

    private final String hostResendEmail = Global.HOST_API + "/activate?email=%s&j_captcha=%s";

    @AfterViews
    protected final void initResetPassword() {
        loginButton.setText("重发激活邮件");
    }

    @Click
    protected final void loginButton() {
        if (!isEnterSuccess()) {
            return;
        }

        String hostReset = String.format(hostResendEmail, getEmail(), getValify());
        getNetwork(hostReset, hostResendEmail);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(hostResendEmail)) {
            if (code == 0) {
                showMiddleToast("重置密码邮件已经发送，请尽快去邮箱查收");
            } else {
                downloadValifyPhoto();
                showErrorMsg(code, respanse);
            }
        }
    }
}
