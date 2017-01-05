package net.coding.program.user;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.tencent.android.tpush.XGPushManager;

import net.coding.program.EnterpriseMainActivity_;
import net.coding.program.MyAppEnterprise;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.SimpleSHA1;
import net.coding.program.common.enter.SimpleTextWatcher;
import net.coding.program.common.guide.GuideActivity;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.util.FileUtil;
import net.coding.program.common.util.InputCheck;
import net.coding.program.common.widget.LoginEditText;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.login.PhoneRegisterActivity_;
import net.coding.program.login.auth.AuthInfo;
import net.coding.program.login.auth.TotpClock;
import net.coding.program.login.phone.EnterpriseEmailSetPasswordActivity_;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.EnterpriseDetail;
import net.coding.program.model.EnterpriseInfo;
import net.coding.program.model.UserObject;
import net.coding.program.third.FastBlur;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import cz.msebera.android.httpclient.Header;

@EActivity(R.layout.enterprise_activity_login)
public class EnterpriseLoginActivity extends BaseActivity {

    private final String TAG_HOST_USER = "TAG_HOST_USER";
    private final String TAG_HOST_ENTERPRISE_DETAIL = "TAG_HOST_ENTERPRISE_DETAIL";
    private final String TAG_HOST_IS_ADMIN = "TAG_HOST_IS_ADMIN";
    private final String TAG_HOST_USER_NEED_2FA = "TAG_HOST_USER_NEED_2FA";

    public static final String EXTRA_BACKGROUND = "background";
    private String TAG_HOST_NEED_CAPTCHA = "TAG_HOST_NEED_CAPTCHA";
    final float radius = 8;
    final double scaleFactor = 16;
    private final String TAG_LOGIN = "TAG_LOGIN";

    final private int RESULT_CLOSE = 100;

    @Extra
    Uri background;

    @ViewById
    LoginEditText enterpriseEdit;

    @ViewById
    ImageView imageValify;
    @ViewById
    LoginEditText editName, editPassword;

    @ViewById
    EditText editValify, edit2FA;
    @ViewById
    View captchaLayout, loginButton, layout2fa, loginLayout, layoutRoot;

    @ViewById
    TextView loginFail;

    private int clickIconCount = 0;
    private long lastClickTime = 0;

    DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.icon_user_monkey)
            .showImageOnFail(R.drawable.icon_user_monkey)
            .resetViewBeforeLoading(true)
            .cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .considerExifParams(true)
            .displayer(new FadeInBitmapDisplayer(300))
            .build();
    View androidContent;
    TextWatcher textWatcher = new SimpleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            upateLoginButton();
        }
    };
    private String globalKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 调用下，防止收到上次登录账号的通知
        XGPushManager.registerPush(this, "*");
    }

    @AfterViews
    void init() {
        needCaptcha();

        editName.addTextChangedListener(textWatcher);
        editPassword.addTextChangedListener(textWatcher);
        editValify.addTextChangedListener(textWatcher);
        upateLoginButton();

        androidContent = findViewById(android.R.id.content);

        String lastLoginName = AccountInfo.loadLastLoginName(this);
        if (!lastLoginName.isEmpty()) {
//            editName.setDisableAuto(true);
            editName.setText(lastLoginName);
//            editName.setDisableAuto(false);
            editPassword.requestFocus();
        }

        loginFail.setText(Global.createBlueHtml("忘记密码？", "找回密码", ""));
    }

    @Click
    void userIcon() {
        long clickTime = Calendar.getInstance().getTimeInMillis();
        long lastTemp = lastClickTime;
        lastClickTime = clickTime;
        if (clickTime - lastTemp < 1000) {
            ++clickIconCount;
        } else {
            clickIconCount = 1;
        }

        if (clickIconCount >= 5) {
            clickIconCount = 0;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View content = getLayoutInflater().inflate(R.layout.host_setting, null);
            final EditText editText = (EditText) content.findViewById(R.id.edit);
            final EditText editCode = (EditText) content.findViewById(R.id.editCode);
            AccountInfo.CustomHost customHost = AccountInfo.getCustomHost(this);
            editText.setText(customHost.getHost());
            editCode.setText(customHost.getCode());
            editText.setHint(Global.DEFAULT_HOST);
            builder.setView(content)
                    .setPositiveButton(R.string.action_ok, (dialog, which) -> {
                        String hostString = editText.getText().toString();
                        String hostCode = editCode.getText().toString();
                        AccountInfo.CustomHost customHost1 = new AccountInfo.CustomHost(hostString, hostCode);
                        if (!hostString.isEmpty()) {
                            AccountInfo.saveCustomHost(this, customHost1);
                        } else {
                            AccountInfo.removeCustomHost(this);
                        }

                        AccountInfo.loginOut(this);
                        setResult(RESULT_OK);
                        finish();
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        }
    }

    private BitmapDrawable createBlur(int bgId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), bgId, options);
        int height = options.outHeight;
        int width = options.outWidth;

        options.outHeight = (int) (height / scaleFactor);
        options.outWidth = (int) (width / scaleFactor);
        options.inSampleSize = (int) (scaleFactor + 0.5);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inMutable = true;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.entrance1, options);
        Bitmap blurBitmap = FastBlur.doBlur(bitmap, (int) radius, true);

        return new BitmapDrawable(getResources(), blurBitmap);
    }

    private BitmapDrawable createBlur(Uri uri) {
        String path = FileUtil.getPath(this, uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int height = options.outHeight;
        int width = options.outWidth;

        options.outHeight = (int) (height / scaleFactor);
        options.outWidth = (int) (width / scaleFactor);
        options.inSampleSize = (int) (scaleFactor + 0.5);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inMutable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        Bitmap blurBitmap = FastBlur.doBlur(bitmap, (int) radius, true);

        return new BitmapDrawable(getResources(), blurBitmap);
    }

    @Click
    void imageValify() {
        editValify.requestFocus();
        downloadValifyPhoto();
    }

    @Click
    void register() {
        Global.popSoftkeyboard(this, editName, false);
        PhoneRegisterActivity_.intent(this)
                .startForResult(RESULT_CLOSE);
    }

    @OnActivityResult(RESULT_CLOSE)
    void resultRegiter(int result) {
        if (result == Activity.RESULT_OK) {
            sendBroadcast(new Intent(GuideActivity.BROADCAST_GUIDE_ACTIVITY));
            finish();
        }
    }

    private void needCaptcha() {
        getNetwork(Global.HOST_API + "/captcha/login", TAG_HOST_NEED_CAPTCHA);
    }

    private void downloadValifyPhoto() {
        String host = Global.HOST_API + "/getCaptcha";
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
    protected final void loginButton() {
        if (layout2fa.getVisibility() == View.GONE) {
            login();
        } else {
            login2fa();
        }
    }

    private void login2fa() {
        String input = edit2FA.getText().toString();
        if (input.isEmpty()) {
            showMiddleToast("请输入身份验证器中的验证码");
            return;
        }

        RequestParams params = new RequestParams();
        params.put("code", input);
        postNetwork(Global.HOST_API + "/check_two_factor_auth_code", params, TAG_HOST_USER_NEED_2FA);
        showProgressBar(true, "登录中");

        Global.popSoftkeyboard(this, edit2FA, false);
    }

    private void login() {
        try {
            String name = editName.getText().toString();
            String password = editPassword.getText().toString();
            String captcha = editValify.getText().toString();

            if (name.isEmpty()) {
                showMiddleToast("邮箱或用户名不能为空");
                return;
            }

            if (password.isEmpty()) {
                showMiddleToast("密码不能为空");
                return;
            }

            RequestParams params = new RequestParams();

            params.put("account", name);
            params.put("password", SimpleSHA1.sha1(password));
            if (captchaLayout.getVisibility() == View.VISIBLE) {
                params.put("j_captcha", captcha);
            }
            params.put("remember_me", true);

            String HOST_LOGIN = String.format("http://%s.coding.test/api/v2/account/login", enterpriseEdit.getTextString());

            postNetwork(HOST_LOGIN, params, TAG_LOGIN);
            showProgressBar(true, R.string.logining);

            Global.popSoftkeyboard(this, editName, false);

        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    @Click
    protected final void loginFail() {
        String account = editName.getText().toString();
        if (!InputCheck.isPhone(account) && !InputCheck.isEmail(account)) {
            account = "";
        }

        EnterpriseEmailSetPasswordActivity_.intent(this)
                .enterpriseName(enterpriseEdit.getTextString())
                .account(account)
                .start();
    }

    @Click
    protected final void login_2fa() {
        Global.start2FAActivity(this);
    }

    private void show2FA(boolean show) {
        if (show) {
            layout2fa.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);
            String uri = AccountInfo.loadAuth(this, globalKey);
            if (!uri.isEmpty()) {
                String code2FA = new AuthInfo(uri, new TotpClock(this)).getCode();
                edit2FA.getText().insert(0, code2FA);
                loginButton();
            }

        } else {
            layout2fa.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        }
    }

    private UserObject currentUserInfo = null;
    private EnterpriseDetail enterpriseDetail = null;

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_LOGIN)) {
            if (code == 0) {
                MyAppEnterprise.setHost(enterpriseEdit.getTextString());
                EnterpriseMainActivity_.setNeedWarnEmailNoValidLogin();
                loginSuccess(respanse);
                umengEvent(UmengEvent.USER, "普通登录");
            } else if (code == 3205) {
                MyAppEnterprise.setHost(enterpriseEdit.getTextString());
                EnterpriseMainActivity_.setNeedWarnEmailNoValidLogin();
                umengEvent(UmengEvent.USER, "2fa登录");
                globalKey = respanse.optJSONObject("msg").optString("two_factor_auth_code_not_empty", "");
                show2FA(true);
                showProgressBar(false);

            } else {
                loginFail(code, respanse, true);
            }

        } else if (tag.equals(TAG_HOST_USER_NEED_2FA)) {
            if (code == 0) {
                loginSuccess(respanse);
            } else {
                loginFail(code, respanse, false);
            }
        } else if (tag.equals(TAG_HOST_USER)) {
            if (code == 0) {
                currentUserInfo = new UserObject(respanse.getJSONObject("data"));

                String url = String.format("%s/team/%s/get", Global.HOST_API, enterpriseEdit.getTextString());
                getNetwork(url, TAG_HOST_ENTERPRISE_DETAIL);
            } else {
                showProgressBar(false);
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_HOST_ENTERPRISE_DETAIL)) {
            if (code == 0) {
                enterpriseDetail = new EnterpriseDetail(respanse.optJSONObject("data"));

                String url = String.format("%s/team/%s/is_admin", Global.HOST_API, enterpriseEdit.getTextString());
                getNetwork(url, TAG_HOST_IS_ADMIN);
            } else {
                showProgressBar(false);
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(TAG_HOST_IS_ADMIN)) { // 判断是否管理员
            if (code == 0) {
                showProgressBar(false);
                enterpriseDetail.isAdmin = respanse.optBoolean("data", false);
                jumpMainActivity();
            } else {
                showProgressBar(false);
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(TAG_HOST_NEED_CAPTCHA)) {
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

    private void jumpMainActivity() throws JSONException {
        EnterpriseInfo.instance().update(this, enterpriseDetail);

        AccountInfo.saveAccount(this, currentUserInfo);
        MyAppEnterprise.sUserObject = currentUserInfo;
        AccountInfo.saveReloginInfo(this, currentUserInfo);

        Global.syncCookie(this);

        String name = editName.getText().toString();
        AccountInfo.saveLastLoginName(this, name);

        sendBroadcast(new Intent(GuideActivity.BROADCAST_GUIDE_ACTIVITY));
        finish();
        startActivity(new Intent(this, CodingCompat.instance().getMainActivity()));
    }

    private void loginFail(int code, JSONObject respanse, boolean needCaptcha) {
        String msg = Global.getErrorMsg(respanse).replaceAll("<li>(.*?)</li>", "\n$1");
        showMiddleToast(msg);
        showProgressBar(false);
        if (code != NetworkImpl.NETWORK_ERROR &&
                code != NetworkImpl.NETWORK_ERROR_SERVICE &&
                needCaptcha) {
            needCaptcha();
        }
    }

    private void loginSuccess(JSONObject respanse) throws JSONException {
        UserObject user = new UserObject(respanse.getJSONObject("data"));

        String HOST_USER = Global.HOST_API + "/user/key/%s";
        getNetwork(String.format(HOST_USER, user.global_key), TAG_HOST_USER);
        showProgressBar(true, R.string.logining);
    }


    @Override
    public void onBackPressed() {
        if (layout2fa.getVisibility() == View.GONE) {
            finish();
        } else {
            show2FA(false);
        }
    }

    private void upateLoginButton() {
        if (editName.getText().length() == 0) {
            loginButton.setEnabled(false);
            return;
        }

        if (editPassword.getText().length() == 0) {
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
}