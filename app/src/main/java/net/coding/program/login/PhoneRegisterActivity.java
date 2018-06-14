package net.coding.program.login;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.LoginActivity_;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.SimpleSHA1;
import net.coding.program.common.TermsActivity_;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.event.EventLoginSuccess;
import net.coding.program.common.model.PhoneCountry;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.InputCheck;
import net.coding.program.common.util.OnTextChange;
import net.coding.program.common.util.SingleToast;
import net.coding.program.common.widget.ValidePhoneView;
import net.coding.program.common.widget.input.SimpleTextWatcher;
import net.coding.program.login.phone.CountryPickActivity_;
import net.coding.program.network.HttpObserver;
import net.coding.program.network.Network;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_phone_register)
public class PhoneRegisterActivity extends BackActivity {

    private static final int RESULT_PICK_COUNTRY = 10;

    public static CharSequence REGIST_TIP = Global.createGreenHtml("点击注册，即同意", "《Coding 服务条款》", "");

    @ViewById
    EditText passwordEditAgain, globalKeyEdit, phoneEdit, passwordEdit, phoneCodeEdit;

    @ViewById
    View firstStep, loginButton;

    @ViewById
    View firstLayout, secondLayout;

    @ViewById
    TextView textClause, countryCode;

    @ViewById
    ValidePhoneView sendCode;

    private PhoneCountry pickCountry = PhoneCountry.getChina();


    SimpleTextWatcher stepButtonEnable = new SimpleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            firstStep.setEnabled(globalKeyEdit.getText().length() > 0 &&
                    phoneEdit.getText().length() > 0 &&
                    phoneCodeEdit.getText().length() > 0);
        }
    };

    @AfterViews
    void initPhoneVerifyFragment() {
        globalKeyEdit.addTextChangedListener(stepButtonEnable);
        phoneEdit.addTextChangedListener(stepButtonEnable);
        phoneCodeEdit.addTextChangedListener(stepButtonEnable);

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

        textClause.setText(REGIST_TIP);

        sendCode.setEditPhone(new OnTextChange() {
            @Override
            public void addTextChangedListener(TextWatcher watcher) {
                phoneEdit.addTextChangedListener(watcher);
            }

            @Override
            public boolean isEmpty() {
                return phoneEdit.getText().toString().isEmpty();
            }

            @Override
            public Editable getText() {
                return phoneEdit.getText();
            }
        });

        sendCode.setType(ValidePhoneView.Type.register);

        bindCountry();
    }

    @Override
    protected boolean userEventBus() {
        return true;
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

    @AfterTextChange({R.id.globalKeyEdit, R.id.phoneEdit, R.id.phoneCodeEdit})
    void validFirstStepButton(TextView tv, Editable text) {
        firstStep.setEnabled(InputCheck.checkEditIsFill(globalKeyEdit, phoneEdit, phoneCodeEdit));
    }

    @AfterTextChange({R.id.passwordEdit, R.id.passwordEditAgain})
    void validRegisterButton(TextView tv, Editable text) {
        loginButton.setEnabled(InputCheck.checkEditIsFill(passwordEdit, passwordEditAgain));
    }

    private void bindCountry() {
        countryCode.setText(pickCountry.getCountryCode());
        sendCode.setPhoneCountry(pickCountry);
    }

    @Override
    public void onStop() {
        sendCode.onStop();
        super.onStop();
    }

    @Click
    void firstStep() {
        showProgressBar(true);

        Network.getRetrofit(this)
                .checkGKRegistered(globalKeyEdit.getText().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Boolean>(this) {
                    @Override
                    public void onSuccess(Boolean data) {
                        super.onSuccess(data);
                        checkMessageCode();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
                        super.onFail(errorCode, error);
                        showProgressBar(false);
                    }
                });
    }

    private void checkMessageCode() {
        showProgressBar(true);

        Network.getRetrofit(this)
                .checkRegisterMessageCode(pickCountry.getCountryCode(), phoneEdit.getText().toString(), phoneCodeEdit.getText().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpObserver<Boolean>(this) {
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
        Global.popSoftkeyboard(this, phoneEdit, false);

        String phone = phoneEdit.getText().toString();
        String code = phoneCodeEdit.getText().toString();
        String globalKeyString = globalKeyEdit.getText().toString();
        String password = passwordEdit.getText().toString();

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
        params.put("phone", phone);
        params.put("global_key", globalKeyString);
        params.put("code", code);

        String sha1 = SimpleSHA1.sha1(password);
        params.put("password", sha1);
        params.put("confirm", sha1);

        params.put("phoneCountryCode", pickCountry.getCountryCode());

        String url = Global.HOST_API + "/v2/account/register?channel=coding-android";
        MyAsyncHttpClient.post(this, url, params, new MyJsonResponse(PhoneRegisterActivity.this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                EmailRegisterActivity.parseRegisterSuccess(PhoneRegisterActivity.this, response);
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                showProgressBar(false);
            }
        });


        showProgressBar(true, "");
    }

    @Click
    void textClause() {
        TermsActivity_.intent(this).start();
    }

    @Click
    void login() {
        finish();
        Global.popSoftkeyboard(this, phoneEdit, false);
        LoginActivity_.intent(this).start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventClose(EventLoginSuccess event) {
        finish();
    }

    @Click
    void backImage() {
        login();
    }

    @OnActivityResult(EmailRegisterActivity.RESULT_REGISTER_EMAIL)
    void resultEmailRegister(int result) {
        if (result == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (firstLayout.getVisibility() != View.VISIBLE) {
            firstLayout.setVisibility(View.VISIBLE);
            secondLayout.setVisibility(View.GONE);
        } else {
            login();
        }
    }
}
