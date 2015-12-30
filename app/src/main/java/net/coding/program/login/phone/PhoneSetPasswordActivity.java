package net.coding.program.login.phone;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;

@EActivity(R.layout.activity_phone_set_password)
public class PhoneSetPasswordActivity extends BackActivity implements ParentActivity {

    public static final int RESULT_REGISTER_EMAIL = 1;

    public static String REGIST_TIP = "注册 Coding 账号表示您已同意<font color=\"#3bbd79\">《coding服务条款》</font>";

    public static enum Type {
        register,
        reset,
        activate;

        public String getSendPhoneMessageUrl() {
            switch (this) {
                case register:
                    return Global.HOST_API + "/account/register/generate_phone_code";
                case reset:
                    return Global.HOST_API + "/account/reset_password/generate_phone_code";
                case activate:
                    return Global.HOST_API + "/account/activate/generate_phone_code";
                default:
                    throw new AssertionError("new type " + this.name());
            }
        }

        public String getSetPasswordPhoneUrl(RequestParams params) {
            switch (this) {
                case register:
                    params.put("channel", "coding-android");
                    return Global.HOST_API + "/account/register/phone";
                case reset:
                    return Global.HOST_API + "/phone/resetPassword";
                case activate:
                    return Global.HOST_API + "/account/activate/phone/set_password";
                default:
                    throw new AssertionError("new type " + this.name());
            }
        }

        public String getResetPasswordEmailUrl() {
            String type = "";
            switch (this) {
                case reset:
                    type = "resetPassword";
                    break;
                case activate:
                    type = "activate";
                    break;
                default: // register
                    throw new AssertionError("new type " + this.name());
            }
            return Global.HOST_API + "/" + type + "?email=%s&j_captcha=%s";
        }

        public String getInputAccountTitle() {
            switch (this) {
                case register:
                    return "注册";
                case reset:
                    return "忘记密码";
                case activate:
                    return "设置密码";
                default: // resetPassword
                    throw new AssertionError("new type " + this.name());
            }
        }

        public String getSetPasswordButtonText() {
            switch (this) {
                case register:
                    return "完成注册";
                case reset:
                    return "重置密码";
                case activate:
                    return "设置密码";
                default:
                    throw new AssertionError("new type " + this.name());
            }
        }

        public String getSetPasswordSuccess() {
            switch (this) {
                case register:
                    return "注册成功";
                case reset:
                    return "重置密码成功";
                case activate:
                    return "设置密码成功";
                default:
                    throw new AssertionError("new type " + this.name());
            }
        }
    }

    @Extra
    Type type = Type.register;

    @Extra
    String account;

    private RequestParams requestParams = new RequestParams();

    @AfterViews
    final void initPhoneSetPasswordActivity() {
        setTitle(type.getInputAccountTitle());
        Fragment fragment;
        if (type == Type.register) {
             fragment = PhoneVerifyFragment_.builder().type(type).account(account).build();
        } else {
            requestParams.put("phone", account);
            fragment = PhoneSetPasswordFragment2_.builder().type(type).account(account).build();
        }
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public RequestParams getRequestParmas() {
        return requestParams;
    }

    @Override
    public void next() {
        Fragment fragment;
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            fragment = PhoneSetGlobalFragment_.builder().build();
        } else {
            fragment = PhoneSetPasswordFragment_.builder().type(type).build();
        }
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 2) {
            new AlertDialog.Builder(this)
                    .setTitle("不激活就无法使用 Coding，确定放弃?")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            finish();
        }
    }

    @OnActivityResult(RESULT_REGISTER_EMAIL)
    void resultEmailRegister(int result) {
        if (result == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

}
