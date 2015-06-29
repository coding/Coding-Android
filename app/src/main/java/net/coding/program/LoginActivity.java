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
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.common.widget.LoginAutoCompleteEdit;
import net.coding.program.login.SendEmailActiveActivity_;
import net.coding.program.login.SendEmailPasswordActivity_;
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
    public static String HOST_USER = Global.HOST + "/api/user/key/%s";
    private static String HOST_NEED_CAPTCHA = Global.HOST + "/api/captcha/login";
    final float radius = 8;
    final double scaleFactor = 16;
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
    View captchaLayout;
    @ViewById
    View loginButton;
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
    String HOST_LOGIN = Global.HOST + "/api/login";
    String HOST_USER_RELOGIN = "HOST_USER_RELOGIN";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 调用下，防止收到上次登陆账号的通知
        XGPushManager.registerPush(this, "*");
    }

    @AfterViews
    void init() {
        if (background == null) {
            LoginBackground.PhotoItem photoItem = new LoginBackground(this).getPhoto();
            File file = photoItem.getCacheFile(this);
            if (file.exists()) {
                background = Uri.fromFile(file);
            }
        }

        try {
            BitmapDrawable bitmapDrawable;
            if (background == null) {
                bitmapDrawable = createBlur();
            } else {
                bitmapDrawable = createBlur(background);
            }
            backgroundImage.setImageDrawable(bitmapDrawable);
        } catch (Exception e) {
            Global.errorLog(e);
        }

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

    private BitmapDrawable createBlur() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.entrance1, options);
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
            setResult(Activity.RESULT_OK);
            finish();
        }
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
    protected final void loginButton() {
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

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_LOGIN)) {
            if (code == 0) {
                UserObject user = new UserObject(respanse.getJSONObject("data"));
                getNetwork(String.format(HOST_USER, user.global_key), HOST_USER);
                showProgressBar(true, R.string.logining);

            } else {
                String msg = Global.getErrorMsg(respanse);
                showMiddleToast(msg);
                showProgressBar(false);
                if (code != NetworkImpl.NETWORK_ERROR &&
                        code != NetworkImpl.NETWORK_ERROR_SERVICE) {
                    needCaptcha();
                }
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

                setResult(Activity.RESULT_OK);
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