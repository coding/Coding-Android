package net.coding.program;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fivehundredpx.android.blur.BlurringView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.orhanobut.logger.Logger;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.LoginBackground;
import net.coding.program.common.SimpleSHA1;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.util.InputCheck;
import net.coding.program.common.widget.LoginAutoCompleteEdit;
import net.coding.program.common.widget.input.SimpleTextWatcher;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.guide.GuideActivity;
import net.coding.program.login.PhoneRegisterActivity_;
import net.coding.program.login.auth.AuthInfo;
import net.coding.program.login.auth.TotpClock;
import net.coding.program.login.phone.Close2FAActivity_;
import net.coding.program.login.phone.InputAccountActivity_;
import net.coding.program.maopao.share.CustomShareBoard;
import net.coding.program.thirdplatform.ThirdPlatformLogin;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FocusChange;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

@EActivity(R.layout.activity_login)
public class LoginActivity extends BaseActivity {

    public static final String EXTRA_BACKGROUND = "background";
    final float radius = 8;
    final double scaleFactor = 16;
    final String HOST_USER_RELOGIN = "HOST_USER_RELOGIN";
    final String HOST_USER_NEED_2FA = Global.HOST_API + "/check_two_factor_auth_code";
    private final String TAG_LOGIN = "TAG_LOGIN";
    private final int RESULT_CLOSE = 100;
    private final int RESULT_CLOSE_2FA = 101;

    private final String CLOSE_2FA = "关闭两步验证";
    public String HOST_USER = Global.HOST_API + "/user/key/%s";
    @Extra
    Uri background;
    @ViewById
    ImageView userIcon, backgroundImage, imageValify;
    @ViewById
    LoginAutoCompleteEdit editName;
    @ViewById
    EditText editPassword, editValify, edit2FA;
    @ViewById
    View captchaLayout, loginButton, layout2fa, loginLayout, layoutRoot;
    @ViewById
    TextView loginFail;
    @ViewById(R.id.blurringView)
    BlurringView blurringView;

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
    TextWatcher textWatcherName = new SimpleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            userIcon.setImageResource(R.drawable.icon_user_monkey);
//            userIcon.setBackgroundResource(R.drawable.icon_user_monkey);
        }
    };
    private String HOST_NEED_CAPTCHA = Global.HOST_API + "/captcha/login";
    private int clickIconCount = 0;
    private long lastClickTime = 0;
    private String globalKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void init() {
        blurringView.setBlurredView(backgroundImage);
        settingBackground();

        needCaptcha();

        editName.addTextChangedListener(textWatcher);
        editPassword.addTextChangedListener(textWatcher);
        editValify.addTextChangedListener(textWatcher);
        upateLoginButton();

        editName.addTextChangedListener(textWatcherName);

        androidContent = findViewById(android.R.id.content);
        androidContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = androidContent.getHeight();
                if (height > 0) {
                    ViewGroup.LayoutParams lp = layoutRoot.getLayoutParams();
                    lp.height = height;
                    layoutRoot.setLayoutParams(lp);
                    androidContent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        String lastLoginName = AccountInfo.loadLastLoginName(this);
        if (!lastLoginName.isEmpty()) {
            editName.setDisableAuto(true);
            editName.setText(lastLoginName);
            editName.setDisableAuto(false);
            editPassword.requestFocus();
            editName(false);
        }
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

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
            View content = getLayoutInflater().inflate(R.layout.host_setting, null);
            final EditText editText = content.findViewById(R.id.edit);
            AccountInfo.CustomHost customHost = AccountInfo.getCustomHost(this);
            editText.setText(customHost.getHost());
            editText.setHint(Global.DEFAULT_HOST);
            builder.setView(content)
                    .setPositiveButton(R.string.action_ok, (dialog, which) -> {
                        String hostString = editText.getText().toString();
                        AccountInfo.CustomHost customHost1 = new AccountInfo.CustomHost(hostString, "");
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

    private void settingBackground() {
        try {
//            if (ZhongQiuGuideActivity.isZhongqiu()) {
//                bitmapDrawable = createBlur(R.drawable.zhongqiu_init_photo);
//            } else {
            if (background == null) {
                LoginBackground.PhotoItem photoItem = new LoginBackground(this).getPhoto();
                File file = photoItem.getCacheFile(this);
                if (file.exists()) {
                    background = Uri.fromFile(file);
                }
            }

            if (background == null) {
                backgroundImage.setImageResource(R.drawable.entrance1);
            } else {
                ImageLoader.getInstance().displayImage(background.toString(), backgroundImage);
            }
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
    void register() {
        if (1 == 1) {

//            final boolean isauth = UMShareAPI.get(mContext).isAuthorize(mActivity, list.get(position).mPlatform);
//            if (isauth) {
//                UMShareAPI.get(mContext).deleteOauth(mActivity, list.get(position).mPlatform, authListener);
//            } else {
            ThirdPlatformLogin.loginByWeixin(this, umAuthListener);
            return;
        }

        Global.popSoftkeyboard(this, editName, false);
        PhoneRegisterActivity_.intent(this)
                .startForResult(RESULT_CLOSE);
    }

    @OnActivityResult(RESULT_CLOSE_2FA)
    void onResultClose2FA() {
        show2FA(false);
    }

    @OnActivityResult(RESULT_CLOSE)
    void resultRegiter(int result) {
        if (result == Activity.RESULT_OK) {
            sendBroadcast(new Intent(GuideActivity.BROADCAST_GUIDE_ACTIVITY));
            finish();
        }
    }

    private void needCaptcha() {
        getNetwork(HOST_NEED_CAPTCHA, HOST_NEED_CAPTCHA);
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
        postNetwork(HOST_USER_NEED_2FA, params, HOST_USER_NEED_2FA);
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

            params.put("password", SimpleSHA1.sha1(password));
            if (captchaLayout.getVisibility() == View.VISIBLE) {
                params.put("j_captcha", captcha);
            }
            params.put("remember_me", true);

            Global.display(this);


            String HOST_LOGIN = Global.HOST_API + "/v2/account/login";
            params.put("account", name);

            postNetwork(HOST_LOGIN, params, TAG_LOGIN);
            showProgressBar(true, R.string.logining);

            Global.popSoftkeyboard(this, editName, false);

        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    @Click
    protected final void loginFail() {
        if (loginFail.getText().toString().equals(CLOSE_2FA)) {
            Close2FAActivity_.intent(this).startForResult(RESULT_CLOSE_2FA);
        } else {
            String account = editName.getText().toString();
            if (!InputCheck.isPhone(account) && !InputCheck.isEmail(account)) {
                account = "";
            }
            InputAccountActivity_.intent(LoginActivity.this).account(account).start();
        }
    }

    @Click
    protected final void login_2fa() {
        GlobalCommon.start2FAActivity(this);
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
            loginFail.setText(CLOSE_2FA);

        } else {
            layout2fa.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);

            loginFail.setText("找回密码");
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_LOGIN)) {
            if (code == 0) {
                MainActivity.setNeedWarnEmailNoValidLogin();
                loginSuccess(respanse);
                umengEvent(UmengEvent.USER, "普通登录");
            } else if (code == 3205) {
                MainActivity.setNeedWarnEmailNoValidLogin();
                umengEvent(UmengEvent.USER, "2fa登录");
                globalKey = respanse.optJSONObject("msg").optString("two_factor_auth_code_not_empty", "");
                show2FA(true);
                showProgressBar(false);

            } else {
                loginFail(code, respanse, true);
            }

        } else if (tag.equals(HOST_USER_NEED_2FA)) {
            if (code == 0) {
                loginSuccess(respanse);
            } else {
                loginFail(code, respanse, false);
            }
        } else if (tag.equals(HOST_USER)) {
            if (code == 0) {
                showProgressBar(false);
                UserObject user = new UserObject(respanse.getJSONObject("data"));
                AccountInfo.saveAccount(this, user);
                GlobalData.sUserObject = user;
                AccountInfo.saveReloginInfo(this, user);

                Global.syncCookie(this);

                String name = editName.getText().toString();
                AccountInfo.saveLastLoginName(this, name);

                sendBroadcast(new Intent(GuideActivity.BROADCAST_GUIDE_ACTIVITY));
                finish();
                startActivity(new Intent(LoginActivity.this, CodingCompat.instance().getMainActivity()));
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
        } else if (tag.equals(HOST_USER_RELOGIN)) {
            if (code == 0) {
                UserObject user = new UserObject(respanse.getJSONObject("data"));
                imagefromNetwork(userIcon, user.avatar, options);
            }
        }
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
        getNetwork(String.format(HOST_USER, user.global_key), HOST_USER);
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

    @FocusChange
    void editName(boolean hasFocus) {
        if (hasFocus) {
            return;
        }

        String name = editName.getText().toString();
        if (name.isEmpty()) {
            return;
        }

        String global = AccountInfo.loadRelogininfo(this, name);
        if (global.isEmpty()) {
            return;
        }

        getNetwork(String.format(HOST_USER, global), HOST_USER_RELOGIN);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    private UMAuthListener umAuthListener = new UMAuthListener() {
        @Override
        public void onStart(SHARE_MEDIA platform) {
            //授权开始的回调
        }

        @Override
        public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
            Toast.makeText(getApplicationContext(), "Authorize succeed", Toast.LENGTH_SHORT).show();
            Logger.d(data);

        }

        @Override
        public void onError(SHARE_MEDIA platform, int action, Throwable t) {
            Toast.makeText(getApplicationContext(), "Authorize fail", Toast.LENGTH_SHORT).show();
            Logger.d(t);
        }

        @Override
        public void onCancel(SHARE_MEDIA platform, int action) {
            Toast.makeText(getApplicationContext(), "Authorize cancel", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onDestroy() {
        CustomShareBoard.onDestory(this);
        super.onDestroy();
    }
}