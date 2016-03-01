package net.coding.program.login.phone;


import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
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
import net.coding.program.common.enter.SimpleTextWatcher;
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

    @ViewById
    LoginEditText phoneEdit, phoneCaptchaEdit, passwordEdit, repasswordEdit;

    @ViewById
    TextView loginButton;

    @ViewById
    ValidePhoneView sendCode;

    @AfterViews
    final void initPhoneSetPasswordFragment() {
        phoneEdit.setText(account);

        ViewStyleUtil.editTextBindButton(loginButton, phoneEdit, phoneCaptchaEdit, passwordEdit,
                repasswordEdit);

        sendCode.setUrl(ValidePhoneView.RESET_SEND_MESSAGE_URL);
        sendCode.setPhoneString(account);

        phoneEdit.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                account = s.toString();
                sendCode.setPhoneString(account);
            }
        });
    }

    @Override
    public void onStop() {
        sendCode.onStop();
        super.onStop();
    }

    @Click
    void loginButton() {
        String phoneCode = phoneCaptchaEdit.getTextString();
        String password = passwordEdit.getTextString();
        String repassword = repasswordEdit.getTextString();

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

        RequestParams params = new RequestParams();
        String url = ValidePhoneView.URL_RESET_PASSWORD;
        String sha1Password = SimpleSHA1.sha1(password);
        params.put("password", sha1Password);
        params.put("confirm", sha1Password);
        params.put("code", phoneCode);
        params.put("account", account);


        MyAsyncHttpClient.post(getActivity(), url, params, new MyJsonResponse(((BaseActivity) getActivity())) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                closeActivity();
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
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

    private void closeActivity() {
        Toast.makeText(getActivity(), "重置密码成功", Toast.LENGTH_SHORT).show();
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }
}
