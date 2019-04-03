package net.coding.program.login;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.MainActivity;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.SimpleSHA1;
import net.coding.program.common.TermsActivity_;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.event.EventLoginSuccess;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.SingleToast;
import net.coding.program.common.util.ViewStyleUtil;
import net.coding.program.common.widget.LoginEditText;
import net.coding.program.compatible.CodingCompat;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

// 2018/1/24 邮箱注册入口已停止使用，还是留着吧，免得又要加回来
@EActivity(R.layout.activity_email_register)
public class EmailRegisterActivity extends BackActivity {

    public static final int RESULT_REGISTER_EMAIL = 1;

    @ViewById
    LoginEditText globalKeyEdit, emailEdit, passwordEdit, captchaEdit;

    @ViewById
    View loginButton;

    @ViewById
    TextView textClause;

    static void parseRegisterSuccess(Activity activity, JSONObject respanse) {
        UserObject user = new UserObject(respanse.optJSONObject("data"));
        AccountInfo.saveAccount(activity, user);
        GlobalData.sUserObject = user;
        AccountInfo.saveReloginInfo(activity, user);

        Global.syncCookie(activity);

        AccountInfo.saveLastLoginName(activity, user.name);

        activity.startActivity(new Intent(activity, CodingCompat.instance().getMainActivity()));

        EventLoginSuccess.Companion.sendMessage();
        activity.setResult(Activity.RESULT_OK);
        activity.finish();
    }

    @AfterViews
    void initPhoneVerifyFragment() {
        View androidContent = findViewById(android.R.id.content);
        androidContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = androidContent.getHeight();
                if (height > 0) {
                    View layoutRoot = findViewById(R.id.layoutRoot);
                    ViewGroup.LayoutParams lp = layoutRoot.getLayoutParams();
                    lp.height = height;
                    layoutRoot.setLayoutParams(lp);
                    androidContent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        ViewStyleUtil.editTextBindButton(loginButton, globalKeyEdit, emailEdit,
                passwordEdit, captchaEdit);

        textClause.setText(PhoneRegisterActivity.REGIST_TIP);

        needShowCaptch();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Click
    void loginButton() {
        Global.popSoftkeyboard(this, emailEdit, false);
        String email = emailEdit.getTextString();
        String globalKeyString = globalKeyEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        String captcha = captchaEdit.getTextString();

        if (globalKeyString.length() < 3) {
            showMiddleToast("用户名（个性后缀）至少为3个字符");
            return;
        }

        if (password.length() < 6) {
            SingleToast.showMiddleToast(this, "密码至少为6位");
            return;
        } else if (64 < password.length()) {
            SingleToast.showMiddleToast(this, "密码不能大于64位");
            return;
        }

        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("global_key", globalKeyString);

        String sha1 = SimpleSHA1.sha1(password);
        params.put("password", sha1);
        params.put("confirm", sha1);

        if (captchaEdit.getVisibility() == View.VISIBLE) {
            params.put("j_captcha", captcha);
        }

        String url = Global.HOST_API + "/v2/account/register?channel=coding-android";
        MyAsyncHttpClient.post(this, url, params, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject respanse) {
                super.onMySuccess(respanse);
                MainActivity.setNeedWarnEmailNoValidRegister();
                parseRegisterSuccess(EmailRegisterActivity.this, respanse);
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
                needShowCaptch();
            }

            @Override
            public void onFinish() {
                super.onFinish();
                showProgressBar(false);
            }
        });


        showProgressBar(true, "");


    }

    private void needShowCaptch() {
        if (captchaEdit.getVisibility() == View.VISIBLE) {
            captchaEdit.requestCaptcha();
            return;
        }

        String HOST_NEED_CAPTCHA = Global.HOST_API + "/captcha/register";
        MyAsyncHttpClient.get(this, HOST_NEED_CAPTCHA, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                if (response.optBoolean("data")) {
                    captchaEdit.setVisibility(View.VISIBLE);
                    captchaEdit.requestCaptcha();
                }
            }
        });
    }

    @Click
    void textClause() {
        TermsActivity_.intent(this).start();
    }

    @Click
    void otherRegister() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.left_to_right);
    }
}
