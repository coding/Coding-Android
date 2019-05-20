package net.coding.program.user;

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

import net.coding.program.EnterpriseApp;
import net.coding.program.EnterpriseMainActivity_;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.GlobalData;
import net.coding.program.common.SimpleSHA1;
import net.coding.program.common.event.EventLoginSuccess;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.EnterpriseDetail;
import net.coding.program.common.model.EnterpriseInfo;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.common.util.InputCheck;
import net.coding.program.common.widget.LoginEditTextNew;
import net.coding.program.common.widget.input.SimpleTextWatcher;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.login.PhoneRegisterActivity_;
import net.coding.program.login.auth.AuthInfo;
import net.coding.program.login.auth.TotpClock;
import net.coding.program.login.phone.Close2FAActivity_;
import net.coding.program.login.phone.EnterpriseEmailSetPasswordActivity_;
import net.coding.program.login.phone.EnterprisePrivateEmailSetPasswordActivity_;
import net.coding.program.network.constant.MemberAuthority;

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

    private static final int RESULT_CLOSE_2FA = 1;

    private final String CLOSE_2FA_TIP = "关闭两步验证";
    private String TAG_HOST_NEED_CAPTCHA = "TAG_HOST_NEED_CAPTCHA";
    private final String TAG_LOGIN = "TAG_LOGIN";
    private final String CE_INFO = "CE_INFO";

    private static final int RESULT_CLOSE = 100;

    private String ssoType = "default";
    private boolean ssoEnabled = false;

    @Extra
    Uri background;

    @Extra
    boolean isPrivate = false;

    @ViewById
    ImageView backButton;

    @ViewById
    TextView login2faMenu, toolbarTitle;

    @ViewById
    LoginEditTextNew enterpriseEdit, privateHost;

    @ViewById
    ImageView imageValifyMain;

    @ViewById
    LoginEditTextNew editName, editPassword, editValifyMain, edit2FA;

    @ViewById
    View enterpriseLine, valifyLineMain, privateHostLayout, hostLayout, normalHostLayout;

    @ViewById
    View captchaLayout, loginButton, layout2fa, loginLayout, layoutRoot;

    @ViewById
    TextView loginFail;

    // 快速点击 5 次弹出对话框进入 staging 环境
    private int clickIconCount = 0;
    private long lastClickTime = 0;

    private String enterpriseGK = "";


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
        XGPushManager.registerPush(this);

        if (AccountInfo.isLogin(this)) {
            String gk = GlobalData.getEnterpriseGK();
            if (gk.equalsIgnoreCase(EnterpriseApp.PRIVATE_GK)) {
                isPrivate = true;
            }
        }
    }

    @AfterViews
    void initEnterpriseLoginActivity() {
        editName.addTextChangedListener(textWatcher);
        editPassword.addTextChangedListener(textWatcher);
        editValifyMain.addTextChangedListener(textWatcher);
        upateLoginButton();

        androidContent = findViewById(android.R.id.content);

        String lastLoginName = AccountInfo.loadLastLoginName(this);
        if (!lastLoginName.isEmpty()) {
            editName.setText(lastLoginName);
            editPassword.requestFocus();
        }

        String lastCompanyName = AccountInfo.loadLastCompanyName(this);
        if (!lastCompanyName.isEmpty()) {
            enterpriseEdit.setText(lastCompanyName);
        }

        if (isPrivate) {
            String lastHost = AccountInfo.loadLastPrivateHost(this);
            privateHost.setText(lastHost);
        }

        enterpriseEdit.setOnEditFocusChange(createEditLineFocus(enterpriseLine));
        editValifyMain.setOnFocusChangeListener(createEditLineFocus(valifyLineMain));

        hostLayout.setVisibility(View.VISIBLE);
        if (isPrivate) {
            normalHostLayout.setVisibility(View.GONE);
            privateHostLayout.setVisibility(View.VISIBLE);
        } else {
            normalHostLayout.setVisibility(View.VISIBLE);
            privateHostLayout.setVisibility(View.GONE);
        }
    }

    @Click
    void clickNext() {
        hostLayout.setVisibility(View.GONE);
        enterpriseGK = enterpriseEdit.getTextString();
        toolbarTitle.setText(String.format("登录到\n%s", enterpriseGK));
    }

    @Click
    void clickPrivateNext() {
        hostLayout.setVisibility(View.GONE);
        enterpriseGK = EnterpriseApp.PRIVATE_GK;

        String input = privateHost.getTextString();
        String divide = "://";
        int pos = input.indexOf(divide);
        if (pos != -1) {
            input = input.substring(pos + divide.length());
        }

        toolbarTitle.setText(String.format("登录到\n%s", input));
    }

    @Click
    void backButton() {
        onBackPressed();
    }

    @Click
    void toolbarTitle() {
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
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        }
    }

    @Click
    void imageValifyMain() {
        editValifyMain.getEditText().requestFocus();
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
            EventLoginSuccess.Companion.sendMessage();
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
                imageValifyMain.setImageBitmap(BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length));
                editValifyMain.setText("");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showMiddleToast("获取验证码失败");
            }
        });
    }

    @Click
    protected final void loginButton() {
        updateGlobalHost();
        getNetwork(Global.HOST_API + "/enterprise/info/ce", CE_INFO);
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

    private void login(boolean sha1) {
        try {
            String name = editName.getTextString();
            String password = editPassword.getTextString();
            String captcha = editValifyMain.getTextString();

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
            if (sha1) {
                params.put("password", SimpleSHA1.sha1(password));
            } else {
                params.put("password", password);
            }
            if (captchaLayout.getVisibility() == View.VISIBLE) {
                params.put("j_captcha", captcha);
            }
            params.put("remember_me", true);

            updateGlobalHost();
            String HOST_LOGIN = String.format("%s/v2/account/login", Global.HOST_API);

            postNetwork(HOST_LOGIN, params, TAG_LOGIN);
            showProgressBar(true, R.string.logining);

            Global.popSoftkeyboard(this, editName, false);

        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    private void login() {
        login(true);
    }

    private void updateGlobalHost() {
        if (isPrivate) {
            EnterpriseApp.setPrivateHost(privateHost.getTextString());
        } else {
            EnterpriseApp.setHost(enterpriseEdit.getTextString());
        }
    }

    @Click
    protected final void loginFail() {
        String account = editName.getText().toString();
        if (!InputCheck.isPhone(account) && !InputCheck.isEmail(account)) {
            account = "";
        }

        if (isPrivate) {
            EnterprisePrivateEmailSetPasswordActivity_.intent(this)
                    .enterpriseName(privateHost.getTextString())
                    .start();
        } else {
            EnterpriseEmailSetPasswordActivity_.intent(this)
                    .enterpriseName(enterpriseGK)
                    .account(account)
                    .start();
        }
    }

    @Click
    void login2faMenu() {
        if (login2faMenu.getText().equals(CLOSE_2FA_TIP)) {
            Close2FAActivity_.intent(this).startForResult(RESULT_CLOSE_2FA);
        } else {
            GlobalCommon.start2FAActivity(this);
        }
    }

    @OnActivityResult(RESULT_CLOSE_2FA)
    void onResultClose2FA(int resultCode) {
        if (resultCode == RESULT_OK) {
            show2FA(false);
        }
    }

    private void show2FA(boolean show) {
        if (show) {
            layout2fa.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);
            String uri = AccountInfo.loadAuth(this, globalKey);
            if (!uri.isEmpty()) {
                String code2FA = new AuthInfo(uri, new TotpClock(this)).getCode();
                edit2FA.getText().clear();
                edit2FA.getText().insert(0, code2FA);
                loginButton();
            }

            loginFail.setVisibility(View.GONE);

            login2faMenu.setText(CLOSE_2FA_TIP);


        } else {
            layout2fa.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);

            loginFail.setVisibility(View.VISIBLE);

            login2faMenu.setText("两步验证");
        }
    }

    private UserObject currentUserInfo = null;
    private EnterpriseDetail enterpriseDetail = null;

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_LOGIN)) {
            if (code == 0) {
                updateGlobalHost();
                EnterpriseMainActivity_.setNeedWarnEmailNoValidLogin();
                loginSuccess(respanse);
                umengEvent(UmengEvent.USER, "普通登录");
                if (isPrivate) {
                    AccountInfo.saveLastPrivateHost(this, Global.HOST);
                }
            } else if (code == 3205) {
                updateGlobalHost();
                EnterpriseMainActivity_.setNeedWarnEmailNoValidLogin();
                umengEvent(UmengEvent.USER, "2fa登录");
                globalKey = respanse.optJSONObject("msg").optString("two_factor_auth_code_not_empty", "");
                show2FA(true);
                showProgressBar(false);

            } else {
                loginFail(code, respanse, true);
            }

        } else if (tag.equals(CE_INFO)) {
            if (code == 0) {
                try {
                    ssoEnabled = respanse.getJSONObject("data").getJSONObject("settings").getBoolean("ssoEnabled");
                    ssoType = respanse.getJSONObject("data").getJSONObject("settings").getString("ssoType");
                } catch (Exception e) {
                    ssoEnabled = false;
                    ssoType = "default";
                }
                if (layout2fa.getVisibility() == View.GONE) {
                    if (ssoEnabled && "ldap".equals(ssoType)) {
                        login(false);
                    } else {
                        login();
                    }
                } else {
                    login2fa();
                }
            }
        } else if (tag.equals(TAG_HOST_USER_NEED_2FA)) {
            if (code == 0) {
                loginSuccess(respanse);
                if (isPrivate) {
                    AccountInfo.saveLastPrivateHost(this, Global.HOST);
                }
            } else {
                loginFail(code, respanse, false);
            }
        } else if (tag.equals(TAG_HOST_USER)) {
            if (code == 0) {
                currentUserInfo = new UserObject(respanse.getJSONObject("data"));

                if (isPrivate) {
                    String url = String.format("%s/team/%s/get", Global.HOST_API, EnterpriseApp.PRIVATE_GK);
                    getNetwork(url, TAG_HOST_ENTERPRISE_DETAIL);
                } else {
                    String url = String.format("%s/team/%s/get", Global.HOST_API, enterpriseGK);
                    getNetwork(url, TAG_HOST_ENTERPRISE_DETAIL);
                }
            } else {
                showProgressBar(false);
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_HOST_ENTERPRISE_DETAIL)) {
            if (code == 0) {
                enterpriseDetail = new EnterpriseDetail(respanse.optJSONObject("data"));

                String url = String.format("%s/team/%s/is_admin", Global.HOST_API, enterpriseGK);
                getNetwork(url, TAG_HOST_IS_ADMIN);
            } else {
                showProgressBar(false);
                showErrorMsg(code, respanse);
            }

        } else if (tag.equals(TAG_HOST_IS_ADMIN)) { // 判断是否管理员
            if (code == 0) {
                showProgressBar(false);
                boolean isAdmin = respanse.optBoolean("data", false);
                if (isAdmin) {
                    if (!EnterpriseInfo.instance().canManagerEnterprise()) {
                        enterpriseDetail.setIdentity(MemberAuthority.manager);
                    }
                }
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
        GlobalData.sUserObject = currentUserInfo;
        AccountInfo.saveReloginInfo(this, currentUserInfo);

        Global.syncCookie(this);

        String name = editName.getText().toString();
        AccountInfo.saveLastLoginName(this, name);

        String companyName = enterpriseGK;
        AccountInfo.saveLastCompanyName(this, companyName);

        EventLoginSuccess.Companion.sendMessage();
        startActivity(new Intent(this, CodingCompat.instance().getMainActivity()));
        finish();
        overridePendingTransition(R.anim.entrance_fade_in, R.anim.entrance_fade_out);
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
            if (hostLayout.getVisibility() != View.VISIBLE) {
                hostLayout.setVisibility(View.VISIBLE);
            } else {
                finish();
            }
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
                editValifyMain.getText().length() == 0) {
            loginButton.setEnabled(false);
            return;
        }

        loginButton.setEnabled(true);
    }
}