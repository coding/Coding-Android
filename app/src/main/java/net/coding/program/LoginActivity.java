package net.coding.program;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import net.coding.program.common.LoginBackground;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FocusChange;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

@EActivity(R.layout.activity_login)
public class LoginActivity extends BaseActivity {

    @Extra
    Uri background;

    @ViewById
    ImageView userIcon;

    @ViewById
    ImageView backgroundImage;

    @ViewById
    EditText editName;

    @ViewById
    EditText editPassword;

    @ViewById
    ImageView imageValify;

    @ViewById
    EditText editValify;

    @ViewById
    View captchaLayout;

    @AfterViews
    void init() {
        if (background != null) {
            backgroundImage.setImageURI(background);
        } else {
            LoginBackground.PhotoItem photoItem = new LoginBackground(this).getPhoto();
            File file = photoItem.getCacheFile(this);
            if (file.exists()) {
                background = Uri.fromFile(file);
                backgroundImage.setImageURI(background);
            }
        }

        getActionBar().hide();
        needCaptcha();
    }

    @Click
    void imageValify() {
        downloadValifyPhoto();
    }

    @Click
    void register() {
        Uri uri = Uri.parse(Global.HOST);
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(it);
    }

    private void needCaptcha() {
        getNetwork(HOST_NEED_CAPTCHA, HOST_NEED_CAPTCHA);
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

    @Click
    void loginButton() {
        try {
            String name = editName.getText().toString();
            String password = editPassword.getText().toString();
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
            params.put("password", Global.sha1(password));
            if (captchaLayout.getVisibility() == View.VISIBLE) {
                params.put("j_captcha", captcha);
            }
            params.put("remember_me", true);

            postNetwork(HOST_LOGIN, params, HOST_LOGIN);
            showProgressBar(true);

        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    String HOST_NEED_CAPTCHA = Global.HOST + "/api/captcha/login";

    String HOST_LOGIN = Global.HOST + "/api/login";

    String HOST_USER = Global.HOST + "/api/user/key/%s";

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_LOGIN)) {
            if (code == 0) {
                JSONObject json = respanse.getJSONObject("data");
                UserObject user = new UserObject(json);
                getNetwork(String.format(HOST_USER, user.global_key), HOST_USER);
                showProgressBar(true);

            } else {
                String msg = Global.getErrorMsg(respanse);
                showMiddleToast(msg);
                needCaptcha();
                showProgressBar(false);
            }

        } else if (tag.equals(HOST_USER)) {
            if (code == 0) {
                showProgressBar(false);
                UserObject user = new UserObject(respanse.getJSONObject("data"));
                AccountInfo.saveAccount(this, user);
                MyApp.sUserObject = user;
                AccountInfo.saveReloginInfo(this, user.email, user.global_key);

                syncCookie();

                finish();
                startActivity(new Intent(LoginActivity.this, MainActivity_.class));
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
        }
    }

    @FocusChange
    void editName() {

    }

    public void syncCookie() {
        PersistentCookieStore cookieStore = new PersistentCookieStore(this);
        List<Cookie> cookies = cookieStore.getCookies();

        CookieManager cookieManager = CookieManager.getInstance();

        for (int i = 0; i < cookies.size(); i++) {
            Cookie eachCookie = cookies.get(i);
            String cookieString = eachCookie.getName() + "=" + eachCookie.getValue();
            cookieManager.setCookie(Global.HOST, cookieString);
        }

        CookieSyncManager.createInstance(this);
                CookieSyncManager.getInstance().sync();
    }
}



