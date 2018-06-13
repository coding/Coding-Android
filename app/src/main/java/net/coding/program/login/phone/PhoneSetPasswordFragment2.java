package net.coding.program.login.phone;


import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.LoginEditText;
import net.coding.program.common.SimpleSHA1;
import net.coding.program.common.TermsActivity_;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.event.EventLoginSuccess;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.PhoneCountry;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.util.InputCheck;
import net.coding.program.common.util.SingleToast;
import net.coding.program.common.widget.ValidePhoneView;
import net.coding.program.common.widget.input.SimpleTextWatcher;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EFragment(R.layout.fragment_phone_set_password4)
public class PhoneSetPasswordFragment2 extends BaseFragment {

    private static final int RESULT_PICK_COUNTRY = 10;
    private static final int RESULT_RESET_BY_EMAIL = 11;


    @FragmentArg
    String account = "";

    @ViewById
    LoginEditText phoneEdit, phoneCodeEdit, passwordEdit, passwordEditAgain;

    @ViewById
    TextView firstStep, loginButton;

    @ViewById
    TextView countryCode;

    @ViewById
    View firstLayout, secondLayout;

    @ViewById
    ValidePhoneView sendCode;

    private PhoneCountry pickCountry = PhoneCountry.getChina();

    @AfterViews
    final void initPhoneSetPasswordFragment() {
        phoneEdit.setText(account);

        sendCode.setType(ValidePhoneView.Type.setPassword);
        sendCode.setPhoneString(account);

        phoneEdit.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                account = s.toString();
                sendCode.setPhoneString(account);
            }
        });

        bindCountry();
    }

    @Override
    public void onStop() {
        sendCode.onStop();
        super.onStop();
    }

    @AfterTextChange({R.id.phoneEdit, R.id.phoneCodeEdit})
    void validFirstStepButton(TextView tv, Editable text) {
        firstStep.setEnabled(InputCheck.checkEditIsFill(phoneEdit, phoneCodeEdit));
    }

    @AfterTextChange({R.id.passwordEdit, R.id.passwordEditAgain})
    void validRegisterButton(TextView tv, Editable text) {
        loginButton.setEnabled(InputCheck.checkEditIsFill(passwordEdit, passwordEditAgain));
    }

    @Click
    void firstStep() {
        showProgressBar(true);

        Network.getRetrofit(getActivity())
                .checkMessageCode(pickCountry.getCountryCode(), phoneEdit.getText().toString(), phoneCodeEdit.getText().toString(), "reset")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Boolean>(getActivity()) {
                    @Override
                    public void onSuccess(Boolean data) {
                        showProgressBar(false);
                        super.onSuccess(data);

                        firstLayout.setVisibility(View.GONE);
                        secondLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        showProgressBar(false);
                    }
                });
    }


    @Click
    void loginButton() {
        String phoneCode = phoneCodeEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        String repassword = passwordEditAgain.getText().toString();

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
        String url = Global.HOST_API + "/account/password/reset";
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

    private void bindCountry() {
        countryCode.setText(pickCountry.getCountryCode());
        sendCode.setPhoneCountry(pickCountry);
    }

    @Click
    void textClause() {
        TermsActivity_.intent(this).start();
    }

    @Click
    void backImage() {
        onBackPressed();
    }

    @Click
    void emailResetPassword() {
        EmailSetPasswordActivity_.intent(this)
                .account("")
                .startForResult(RESULT_RESET_BY_EMAIL);
    }

    @Click
    void countryCode() {
        CountryPickActivity_.intent(this)
                .startForResult(RESULT_PICK_COUNTRY);
    }

    @OnActivityResult(RESULT_PICK_COUNTRY)
    void onResultPickCountry(int resultCode, @OnActivityResult.Extra PhoneCountry resultData) {
        if (resultCode == Activity.RESULT_OK && resultData != null) {
            pickCountry = resultData;
            bindCountry();
        }
    }

    @OnActivityResult(RESULT_RESET_BY_EMAIL)
    void onResultResetByEmail(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            getActivity().finish();
        }
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
                GlobalData.sUserObject = user;
                AccountInfo.saveReloginInfo(getActivity(), user);

                Global.syncCookie(getActivity());

                AccountInfo.saveLastLoginName(getActivity(), user.name);

                EventLoginSuccess.Companion.sendMessage();
                getActivity().finish();
                startActivity(new Intent(getActivity(), CodingCompat.instance().getMainActivity()));
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
                showProgressBar(false, "");
            }
        });
    }

    public void onBackPressed() {
        if (firstLayout.getVisibility() != View.VISIBLE) {
            firstLayout.setVisibility(View.VISIBLE);
            secondLayout.setVisibility(View.GONE);
        } else {
            getActivity().finish();
        }
    }

    private void closeActivity() {
        Toast.makeText(getActivity(), "重置密码成功", Toast.LENGTH_SHORT).show();
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }
}
