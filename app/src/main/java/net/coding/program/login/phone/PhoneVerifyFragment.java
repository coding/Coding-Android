package net.coding.program.login.phone;

import android.content.Intent;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.RegisterActivity_;
import net.coding.program.common.Global;
import net.coding.program.common.TermsActivity;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.util.ViewStyleUtil;
import net.coding.program.common.widget.LoginEditText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

@EFragment(R.layout.fragment_phone_set_password)
@OptionsMenu(R.menu.menu_register_phone)
public class PhoneVerifyFragment extends BaseFragment {

    @FragmentArg
    PhoneSetPasswordActivity.Type type;

    @FragmentArg
    String account = "";

    @ViewById
    LoginEditText emailEdit, captchaEdit;

    @ViewById
    View loginButton;

    @ViewById
    TextView sendCode, textClause;

    @AfterViews
    void initPhoneVerifyFragment() {
        emailEdit.setText(account);

        ViewStyleUtil.editTextBindButton(loginButton, emailEdit, captchaEdit);

        if (type == PhoneSetPasswordActivity.Type.register) {
            textClause.setText(Html.fromHtml(PhoneSetPasswordActivity.REGIST_TIP));
        }
    }

    @Click
    void loginButton() {
        // TODO
//        if (1 == 1) {
//            ParentActivity parent = (ParentActivity) getActivity();
//            parent.next();
//            return;
//        }

        String phone = emailEdit.getTextString();
        String code = captchaEdit.getTextString();

        String url = Global.HOST_API + "/account/register/check_phone_code";
        RequestParams params = new RequestParams();
        params.put("phone", phone);
        params.put("code", code);
        params.put("type", type.name());
        MyAsyncHttpClient.post(getActivity(), url, params, new MyJsonResponse(getActivity()) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                ParentActivity parent = (ParentActivity) getActivity();
                RequestParams rp = parent.getRequestParmas();
                rp.put("phone", phone);
                rp.put("code", code);
                parent.next();
            }

            @Override
            public void onFinish() {
                super.onFinish();
                showProgressBar(false, "");
            }
        });

        showProgressBar(true, "");
    }

    @Click
    void sendCode() {
        String phone = emailEdit.getTextString();
        if (!InputCheck.checkPhone(getActivity(), phone)) return;

        String url = type.getSendPhoneMessageUrl();
        RequestParams params = new RequestParams();
        params.put("phone", phone);
        MyAsyncHttpClient.post(getActivity(), url, params, new MyJsonResponse(getActivity()) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                showMiddleToast("已发送短信");
            }
        });

        sendCode.setEnabled(false);
        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                sendCode.setText(millisUntilFinished / 1000 + "秒");
            }

            public void onFinish() {
                sendCode.setEnabled(true);
                sendCode.setText("发送验证码");
            }
        }.start();

        captchaEdit.requestFocus();
    }

    @Click
    void textClause() {
        Intent intent = new Intent(getActivity(), TermsActivity.class);
        startActivity(intent);
    }

    @OptionsItem
    void action_email() {
        RegisterActivity_.intent(getActivity())
                .startForResult(PhoneSetPasswordActivity.RESULT_REGISTER_EMAIL);
    }
}
