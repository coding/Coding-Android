package net.coding.program.login;

import android.net.Uri;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.RequestParams;

import net.coding.program.common.ui.BaseActivity;
import net.coding.program.LoginActivity_;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.SimpleSHA1;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_reset_password_base)
public abstract class ResetPasswordBaseActivity extends BaseActivity {

    private final String HOST_REQUEST_TAG = "HOST_REQUEST_TAG";
    @Extra
    protected String link = "";
    @ViewById
    protected EditText editName;
    @ViewById
    protected EditText editPassword;
    @ViewById
    protected EditText editPasswordConfirm;
    @ViewById
    protected Button loginButton;

    @AfterViews
    protected final void testInit() {
        String key = getDataFromIntent("key");
        if (key.isEmpty()) {
            showMiddleToast("链接没有带key");
        }

        String email = getDataFromIntent("email");
        if (email.isEmpty()) {
            showMiddleToast("链接没有带Email");
        } else {
            editName.setText(email);
        }
    }

    @Click
    protected final void loginButton() {
        if (!checkFormat()) {
            return;
        }

        String key = getDataFromIntent("key");
        if (key.isEmpty()) {
            showMiddleToast("链接没有带key");
            return;
        }

        String sha1Password = getSHA1Password();

        RequestParams params = new RequestParams();
        params.add("email", getEmail());
        params.add("key", key);
        params.add("password", sha1Password);
        params.add("confirm_password", sha1Password);
        postNetwork(getRequestHost(), params, HOST_REQUEST_TAG);
    }

    private String getDataFromIntent(String key1) {
        Uri uri = Uri.parse(link);
        String key = uri.getQueryParameter(key1);
        if (key == null) {
            key = "";
        }

        return key;
    }

    abstract String getRequestHost();

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_REQUEST_TAG)) {
            if (code == 0) {
                finish();
                LoginActivity_.intent(this).start();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @AfterViews
    protected final void initResetPasswordBase() {
        editPassword.setHint("密码");
        editPasswordConfirm.setHint("重复密码");
        loginButton.setText("确定");
    }

    protected boolean checkFormat() {
        String email = getEmail();
        if (!SendEmailBaseActivity.isValifyEmail(this, email)) {
            return false;
        }

        String password = editPassword.getText().toString();
        String passwordConfirm = editPasswordConfirm.getText().toString();
        return !passwordFormatError(password, passwordConfirm);
    }

    boolean passwordFormatError(String newPwd, String confirmPwd) {
        if (TextUtils.isEmpty(newPwd)) {
            showMiddleToast("新密码不能为空");
            return true;

        } else if (TextUtils.isEmpty(confirmPwd)) {
            showMiddleToast("确认密码不能为空");
            return true;

        } else if (!newPwd.equals(confirmPwd)) {
            showMiddleToast("两次密码输入不一致");
            return true;

        } else if (newPwd.length() < 6) {
            showMiddleToast("密码不能少于6位");
            return true;

        } else if (newPwd.length() > 64) {
            showMiddleToast("密码不能大于64位");
            return true;
        }

        return false;
    }

    protected String getSHA1Password() {
        String sha1 = "";
        try {
            sha1 = SimpleSHA1.sha1(editPassword.getText().toString());
        } catch (Exception e) {
            Global.errorLog(e);
        }
        return sha1;
    }

    protected String getEmail() {
        return editName.getText().toString();
    }

}
