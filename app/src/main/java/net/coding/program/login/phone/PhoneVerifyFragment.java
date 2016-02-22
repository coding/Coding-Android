package net.coding.program.login.phone;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.util.ActivityNavigate;
import net.coding.program.common.util.ViewStyleUtil;
import net.coding.program.common.widget.LoginEditText;
import net.coding.program.common.widget.ValidePhoneView;

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
    TextView textClause;

    @ViewById
    ValidePhoneView sendCode;

    @AfterViews
    void initPhoneVerifyFragment() {
        emailEdit.setText(account);

        ViewStyleUtil.editTextBindButton(loginButton, emailEdit, captchaEdit);

        if (type == PhoneSetPasswordActivity.Type.register) {
            textClause.setText(Html.fromHtml(PhoneSetPasswordActivity.REGIST_TIP));
        }

        sendCode.setEditPhone(emailEdit);
        sendCode.setUrl(type.getSendPhoneMessageUrl());
    }

    @Override
    public void onStop() {
        sendCode.onStop();
        super.onStop();
    }

    @Click
    void loginButton() {
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
    void textClause() {
        ActivityNavigate.startTermActivity(this);
    }

    @OptionsItem
    void action_email() {
//        RegisterActivity_.intent(getActivity())
//                .startForResult(PhoneSetPasswordActivity.RESULT_REGISTER_EMAIL);
    }
}
