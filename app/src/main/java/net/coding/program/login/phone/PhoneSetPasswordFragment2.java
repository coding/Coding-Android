package net.coding.program.login.phone;


import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

import net.coding.program.MainActivity_;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.SimpleSHA1;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.guide.GuideActivity;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.util.ActivityNavigate;
import net.coding.program.common.util.SingleToast;
import net.coding.program.common.util.ViewStyleUtil;
import net.coding.program.common.widget.LoginEditText;
import net.coding.program.common.widget.ValidePhoneView;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

@EFragment(R.layout.fragment_phone_set_password4)
public class PhoneSetPasswordFragment2 extends BaseFragment {

    @FragmentArg
    String account = "";

    @FragmentArg
    PhoneSetPasswordActivity.Type type;

    @ViewById
    LoginEditText phoneCaptchaEdit, passwordEdit, repasswordEdit, captchaEdit;

    @ViewById
    TextView fragmentTitle, loginButton, textClause;

    @ViewById
    ValidePhoneView sendCode;


    @AfterViews
    final void initPhoneSetPasswordFragment() {
        String title = String.format("我们已经发送了验证码到手机<br/><font color=\"#3BBD79\">%s</font>", account);
        fragmentTitle.setText(Html.fromHtml(title));
        loginButton.setText(type.getSetPasswordButtonText());

        ViewStyleUtil.editTextBindButton(loginButton, phoneCaptchaEdit, passwordEdit, repasswordEdit, captchaEdit);
        needShowCaptch();

        if (type == PhoneSetPasswordActivity.Type.register) {
            textClause.setText(Html.fromHtml(PhoneSetPasswordActivity.REGIST_TIP));
        }

        sendCode.setUrl(type.getSendPhoneMessageUrl());
        sendCode.setPhoneString(account);
        sendCode.startTimer();
    }

    @Override
    public void onStop() {
        sendCode.onStop();
        super.onStop();
    }

    private void needShowCaptch() {
        if (type != PhoneSetPasswordActivity.Type.register) {
            captchaEdit.setVisibility(View.VISIBLE);
            captchaEdit.requestCaptcha();
            return;
        }

        if (captchaEdit.getVisibility() == View.VISIBLE) {
            captchaEdit.requestCaptcha();
            return;
        }

        String HOST_NEED_CAPTCHA = Global.HOST_API + "/captcha/register";
        MyAsyncHttpClient.get(getActivity(), HOST_NEED_CAPTCHA, new MyJsonResponse(getActivity()) {
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
    void loginButton() {
        String phoneCode = phoneCaptchaEdit.getTextString();
        String password = passwordEdit.getTextString();
        String repassword = repasswordEdit.getTextString();
        String captcha = captchaEdit.getTextString();

        if (password.length() < 6) {
            SingleToast.showMiddleToast(getActivity(), "密码至少为6位");
            return;
        } else if (64 < password.length()) {
            SingleToast.showMiddleToast(getActivity(), "密码不能大于64位");
            return;
        } else if (!password.equals(repassword)) {
            SingleToast.showMiddleToast(getActivity(), "两次输入的密码不一致");
            return;
        }

        RequestParams params = ((ParentActivity) getActivity()).getRequestParmas();
        String url = type.getSetPasswordPhoneUrl(params);
        params.put("password", SimpleSHA1.sha1(password));
        params.put("j_captcha", captcha);
        params.put("code", phoneCode);

        MyAsyncHttpClient.post(getActivity(), url, params, new MyJsonResponse(((BaseActivity) getActivity())) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);

                if (type == PhoneSetPasswordActivity.Type.register) {
//                    loadCurrentUser();
                    ((ParentActivity) getActivity()).next();
                    showProgressBar(false, "");
                } else {
                    closeActivity();
                }
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
                needShowCaptch();
                showProgressBar(false, "");
            }
        });

        showProgressBar(true, "");
    }

    @Click
    void textClause() {
        ActivityNavigate.startTermActivity(this);
    }

    protected void loadCurrentUser() {
        AsyncHttpClient client = MyAsyncHttpClient.createClient(getActivity());
        String url = Global.HOST_API + "/current_user";
        client.get(getActivity(), url, new MyJsonResponse(getActivity()) {

            @Override
            public void onMySuccess(JSONObject respanse) {
                super.onMySuccess(respanse);
//                showProgressBar(false);
                UserObject user = new UserObject(respanse.optJSONObject("data"));
                AccountInfo.saveAccount(getActivity(), user);
                MyApp.sUserObject = user;
                AccountInfo.saveReloginInfo(getActivity(), user);

                Global.syncCookie(getActivity());

                AccountInfo.saveLastLoginName(getActivity(), user.name);

                getActivity().sendBroadcast(new Intent(GuideActivity.BROADCAST_GUIDE_ACTIVITY));
                getActivity().finish();
                startActivity(new Intent(getActivity(), MainActivity_.class));
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
                showProgressBar(false, "");
            }
        });
    }

//    protected void loadUserinfo() {
//        AsyncHttpClient client = MyAsyncHttpClient.createClient(getActivity());
//        String url = Global.HOST_API + "/userinfo";
//        client.get(getActivity(), url, new MyJsonResponse(getActivity()) {
//            @Override
//            public void onMySuccess(JSONObject response) {
//                super.onMySuccess(response);
//                MyData.getInstance().update(getActivity(), response.optJSONObject("data"));
//                closeActivity();
//            }
//
//            @Override
//            public void onFinish() {
//                super.onFinish();
//                ((BaseActivity) getActivity()).showProgressBar(false, "");
//            }
//        });
//    }

    private void closeActivity() {
        Toast.makeText(getActivity(), type.getSetPasswordSuccess(), Toast.LENGTH_SHORT).show();
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }
}
