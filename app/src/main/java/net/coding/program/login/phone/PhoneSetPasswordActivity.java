package net.coding.program.login.phone;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.widget.ValidePhoneView;
import net.coding.program.model.RequestData;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;

// // FIXME: 16/2/22 delete
@EActivity(R.layout.activity_phone_set_password)
public class PhoneSetPasswordActivity extends BackActivity implements ParentActivity {

    public static final int RESULT_REGISTER_EMAIL = 1;

    public static String REGIST_TIP = "注册 Coding 账号表示您已同意<font color=\"#3bbd79\">《Coding 服务条款》</font>";

    public static enum Type {
        register,
        reset,
        activate;

        public String getSendPhoneMessageUrl() {
            switch (this) {
                // // TODO 是否 api 都要改
                case register:
                    return ValidePhoneView.REGISTER_SEND_MESSAGE_URL;
                case reset:
                    return ValidePhoneView.RESET_SEND_MESSAGE_URL;
                case activate:
                    return Global.HOST_API + "/account/activate/generate_phone_code";
                default:
                    throw new AssertionError("new type " + this.name());
            }
        }

        public String getSetPasswordPhoneUrl(RequestParams params) {
            switch (this) {
                // // TODO 是否 api 都要改
                case register:
                    params.put("channel", "coding-android");
                    return Global.HOST_API + "/account/register/phone";
                case reset:
                    return Global.HOST_API + "/account/password/reset";
                case activate:
                    return Global.HOST_API + "/account/activate/phone/set_password";
                default:
                    throw new AssertionError("new type " + this.name());
            }
        }

        public RequestData getResetPasswordEmailUrl(String email, String captcha) {
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

            String url = Global.HOST_API + "/" + type;
            RequestParams params = new RequestParams();
            params.put("email", email);
            params.put("j_captcha", captcha);
            return new RequestData(url, params);
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
        requestParams.put("phone", account);
        fragment = PhoneSetPasswordFragment2_.builder().type(type).account(account).build();
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
