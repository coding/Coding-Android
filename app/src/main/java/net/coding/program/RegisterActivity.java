package net.coding.program;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import net.coding.program.common.Global;
import net.coding.program.common.TermsActivity;
import net.coding.program.common.enter.SimpleTextWatcher;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@EActivity(R.layout.activity_register)
public class RegisterActivity extends BaseAnnotationActivity {

    @ViewById
    EditText editName;

    @ViewById
    EditText editGlobal;

    @ViewById
    ImageView imageValify;

    @ViewById
    EditText editValify;

    @ViewById
    View captchaLayout;

    @ViewById
    View loginButton;

    @ViewById
    TextView textClause;

    @AfterViews
    void init() {
        textClause.setText(Html.fromHtml("点击立即体验，即表示同意<font color=\"#3bbd79\">《coding服务条款》</font>"));
        editName.addTextChangedListener(textWatcher);
        editGlobal.addTextChangedListener(textWatcher);
        editValify.addTextChangedListener(textWatcher);
        upateLoginButton();
        needCaptcha();
    }

    private void needCaptcha() {
        getNetwork(HOST_NEED_CAPTCHA, HOST_NEED_CAPTCHA);
    }

    String HOST_USER_REGISTER = Global.HOST + "/api/register";

    @Click
    void loginButton() {
        try {
            String name = editName.getText().toString();
            String password = editGlobal.getText().toString();
            String captcha = editValify.getText().toString();

            if (name.isEmpty()) {
                showMiddleToast("邮箱或个性后缀不能为空");
                return;
            }

            if (password.isEmpty()) {
                showMiddleToast("密码不能为空");
                return;
            }

            RequestParams params = new RequestParams();
            params.put("email", name);
            params.put("global_key", password);
            if (captchaLayout.getVisibility() == View.VISIBLE) {
                params.put("j_captcha", captcha);
            }
            params.put("remember_me", true);

            postNetwork(HOST_USER_REGISTER, params, HOST_USER_REGISTER);
            showProgressBar(true);

        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    @Click
    void imageValify() {
        editValify.requestFocus();
        downloadValifyPhoto();
    }

    @Click
    void textClause() {
        Intent intent = new Intent(this, TermsActivity.class);
        startActivity(intent);
    }

    private static String HOST_NEED_CAPTCHA = Global.HOST + "/api/captcha/register";

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_USER_REGISTER)) {
            if (code == 0) {
                UserObject user = new UserObject(respanse.getJSONObject("data"));
                getNetwork(String.format(LoginActivity.HOST_USER, user.global_key), LoginActivity.HOST_USER);
                showProgressBar(true);

            } else {
                String msg = Global.getErrorMsg(respanse);
                showMiddleToast(msg);
                needCaptcha();
                showProgressBar(false);
            }

        } else if (tag.equals(LoginActivity.HOST_USER)) {
            if (code == 0) {
                showProgressBar(false);
                UserObject user = new UserObject(respanse.getJSONObject("data"));
                AccountInfo.saveAccount(this, user);
                MyApp.sUserObject = user;
                AccountInfo.saveReloginInfo(this, user.email, user.global_key);

                Global.syncCookie(this);

                setResult(RESULT_OK);
                finish();
                startActivity(new Intent(this, MainActivity_.class));
                Toast.makeText(this, "欢迎注册 Coding，请尽快去邮箱查收邮件并激活账号。如若在收件箱中未看到激活邮件，请留意一下垃圾邮件箱(T_T)。", Toast.LENGTH_LONG).show();
            } else {
                showProgressBar(false);
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(HOST_NEED_CAPTCHA)) {
            if (code == 0) {
                if (respanse.getBoolean("data")) {
                    captchaLayout.setVisibility(View.VISIBLE);
                    downloadValifyPhoto();
                }
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(HOST_USER_REGISTER)) {
            if (code == 0) {
                UserObject user = new UserObject(respanse.getJSONObject("data"));
            }
        }
    }

    TextWatcher textWatcher = new SimpleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            upateLoginButton();
        }
    };

    private void upateLoginButton() {
        if (editName.getText().length() == 0) {
            loginButton.setEnabled(false);
            return;
        }

        if (editGlobal.getText().length() == 0) {
            loginButton.setEnabled(false);
            return;
        }

        if (captchaLayout.getVisibility() == View.VISIBLE &&
                editValify.getText().length() == 0) {
            loginButton.setEnabled(false);
            return;
        }

        loginButton.setEnabled(true);
    }

    private void downloadValifyPhoto() {
        String host = Global.HOST + "/api/getCaptcha";
        AsyncHttpClient client = MyAsyncHttpClient.createClient(this);

        client.get(host, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                imageValify.setImageBitmap(BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length));
                editValify.setText("");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showMiddleToast("获取验证码失败");
            }
        });
    }

}
