package net.coding.program;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.tencent.android.tpush.XGPushManager;

import net.coding.program.common.Global;
import net.coding.program.common.LoginBackground;
import net.coding.program.common.SimpleSHA1;
import net.coding.program.common.enter.SimpleTextWatcher;
import net.coding.program.common.guide.GuideActivity;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.widget.LoginAutoCompleteEdit;
import net.coding.program.login.SendEmailActiveActivity_;
import net.coding.program.login.SendEmailPasswordActivity_;
import net.coding.program.login.ZhongQiuGuideActivity;
import net.coding.program.login.auth.AuthInfo;
import net.coding.program.login.auth.TotpClock;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.UserObject;
import net.coding.program.third.FastBlur;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FocusChange;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

@EActivity(R.layout.activity_login)
public class LoginActivity extends BaseActivity {

    public static final String EXTRA_BACKGROUND = "background";
    public static String HOST_USER = Global.HOST_API + "/user/key/%s";
    private static String HOST_NEED_CAPTCHA = Global.HOST_API + "/captcha/login";
    final float radius = 8;
    final double scaleFactor = 16;
    final String HOST_LOGIN = Global.HOST_API + "/login";
    final String HOST_USER_RELOGIN = "HOST_USER_RELOGIN";
    final String HOST_USER_NEED_2FA = Global.HOST_API + "/check_two_factor_auth_code";
    final private int RESULT_CLOSE = 100;
    @Extra
    Uri background;
    @ViewById
    ImageView userIcon;
    @ViewById
    ImageView backgroundImage;
    @ViewById
    View layoutRoot;
    @ViewById
    LoginAutoCompleteEdit editName;
    @ViewById
    EditText editPassword;
    @ViewById
    ImageView imageValify;
    @ViewById
    EditText editValify;
    @ViewById
    EditText edit2FA;
    @ViewById
    View captchaLayout;
    @ViewById
    View loginButton;
    @ViewById
    View layout2fa, loginLayout;
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
    private String globalKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 调用下，防止收到上次登陆账号的通知
        XGPushManager.registerPush(this, "*");
    }

    @AfterViews
    void init() {
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

    private void settingBackground() {
        try {
            BitmapDrawable bitmapDrawable;
            if (ZhongQiuGuideActivity.isZhongqiu()) {
                bitmapDrawable = createBlur(R.drawable.zhongqiu_init_photo);
            } else {
                if (background == null) {
                    LoginBackground.PhotoItem photoItem = new LoginBackground(this).getPhoto();
                    File file = photoItem.getCacheFile(this);
                    if (file.exists()) {
                        background = Uri.fromFile(file);
                    }
                }

                if (background == null) {
                    bitmapDrawable = createBlur(R.drawable.entrance1);
                } else {
                    bitmapDrawable = createBlur(background);
                }
            }
            backgroundImage.setImageDrawable(bitmapDrawable);
        } catch (Exception e) {
            Global.errorLog(e);
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
        String path = Global.getPath(this, uri);
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
        RegisterActivity_.intent(this).startForResult(RESULT_CLOSE);
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
    }

    private void login() {
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
            params.put("password", SimpleSHA1.sha1(password));
            if (captchaLayout.getVisibility() == View.VISIBLE) {
                params.put("j_captcha", captcha);
            }
            params.put("remember_me", true);

            postNetwork(HOST_LOGIN, params, HOST_LOGIN);
            showProgressBar(true, R.string.logining);

        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    @Click
    protected final void loginFail() {
        String[] listTitles = getResources().getStringArray(R.array.dialog_login_fail_help);
        new AlertDialog.Builder(this).setItems(listTitles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    SendEmailPasswordActivity_
                            .intent(LoginActivity.this)
                            .start();
//                    ResetPasswordActivity_
//                            .intent(LoginActivity.this)
//                            .start();

                } else if (which == 1) {
                    SendEmailActiveActivity_
                            .intent(LoginActivity.this)
                            .start();
//                    UserActiveActivity_
//                            .intent(LoginActivity.this)
//                            .start();
                }
            }
        }).show();
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

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_LOGIN)) {
            if (code == 0) {
                loginSuccess(respanse);
                umengEvent(UmengEvent.USER, "普通登陆");
            } else if (code == 3205) {
                umengEvent(UmengEvent.USER, "2fa登陆");
                globalKey = respanse.optJSONObject("msg").optString("two_factor_auth_code_not_empty", "");
                show2FA(true);
                showProgressBar(false);

            } else {
                loginFail(code, respanse);
            }

        } else if (tag.equals(HOST_USER_NEED_2FA)) {
            if (code == 0) {
                loginSuccess(respanse);
            } else {
                loginFail(code, respanse);
            }
        } else if (tag.equals(HOST_USER)) {
            if (code == 0) {
                showProgressBar(false);
                UserObject user = new UserObject(respanse.getJSONObject("data"));
                AccountInfo.saveAccount(this, user);
                MyApp.sUserObject = user;
                AccountInfo.saveReloginInfo(this, user.email, user.global_key);

                Global.syncCookie(this);

                String name = editName.getText().toString();
                AccountInfo.saveLastLoginName(this, name);

                sendBroadcast(new Intent(GuideActivity.BROADCAST_GUIDE_ACTIVITY));
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
        } else if (tag.equals(HOST_USER_RELOGIN)) {
            if (code == 0) {
                UserObject user = new UserObject(respanse.getJSONObject("data"));
                imagefromNetwork(userIcon, user.avatar, options);
            }
        }
    }

    private void loginFail(int code, JSONObject respanse) {
        String msg = Global.getErrorMsg(respanse);
        showMiddleToast(msg);
        showProgressBar(false);
        if (code != NetworkImpl.NETWORK_ERROR &&
                code != NetworkImpl.NETWORK_ERROR_SERVICE) {
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
}