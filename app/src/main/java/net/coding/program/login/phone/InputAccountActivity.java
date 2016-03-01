package net.coding.program.login.phone;

import android.view.View;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.InputCheck;
import net.coding.program.common.util.InputRequest;
import net.coding.program.common.util.ViewStyleUtil;
import net.coding.program.common.widget.LoginEditText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_input_account)
public class InputAccountActivity extends BackActivity {

    private static final int RESULT_SET_PASSWORD = 1;

    @Extra
    String account = "";

    @ViewById
    LoginEditText accountEdit;

    @ViewById
    View loginButton;

    InputRequest inputRequest = s -> InputCheck.isEmail(s) || InputCheck.isPhone(s);

    @AfterViews
    void initInputAccountActivity() {
        accountEdit.setText(account);
        if (inputRequest.isCurrectFormat(account)) {
            loginButton.setEnabled(true);
        }

        ViewStyleUtil.editTextBindButton(loginButton, inputRequest, accountEdit);
    }

    @Click
    void loginButton() {
        String account = accountEdit.getTextString();
        if (InputCheck.isEmail(account)) {
            EmailSetPasswordActivity_.intent(this)
                    .account(account)
                    .startForResult(RESULT_SET_PASSWORD);
        } else if (InputCheck.isPhone(account)) {
            sendCode(account);
        } else {
            showMiddleToast("输入格式有误");
        }
    }

    void sendCode(String phone) {
        if (!InputCheck.checkPhone(InputAccountActivity.this, phone)) return;

//        String url = ValidePhoneView.RESET_SEND_MESSAGE_URL;
//        RequestParams params = new RequestParams();
//        params.put("account", phone);
//        MyAsyncHttpClient.post(InputAccountActivity.this, url, params, new MyJsonResponse(InputAccountActivity.this) {
//            @Override
//            public void onMySuccess(JSONObject response) {
//                super.onMySuccess(response);
//                showMiddleToast("已发送短信");
//                PhoneSetPasswordActivity_.intent(InputAccountActivity.this)
//                        .account(phone)
//                        .startForResult(RESULT_SET_PASSWORD);
//            }
//
//            @Override
//            public void onFinish() {
//                super.onFinish();
//                showProgressBar(false, "");
//            }
//        });
//
//        showProgressBar(true, "");
        PhoneSetPasswordActivity_.intent(InputAccountActivity.this)
                .account(phone)
                .startForResult(RESULT_SET_PASSWORD);
    }


    @OnActivityResult(RESULT_SET_PASSWORD)
    void onResultSetPassword(int resultCode) {
        if (resultCode == RESULT_OK) {
            setResult(resultCode);
            finish();
        }
    }
}
