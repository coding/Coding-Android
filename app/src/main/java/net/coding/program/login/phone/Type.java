package net.coding.program.login.phone;

import com.loopj.android.http.RequestParams;

import net.coding.program.common.Global;
import net.coding.program.common.widget.ValidePhoneView;
import net.coding.program.model.RequestData;

/**
 * Created by chenchao on 16/2/22.
 */
// // FIXME: 16/2/22 删除
public enum Type {
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
