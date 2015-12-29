package net.coding.program.login.phone;

import android.view.View;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;
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
    PhoneSetPasswordActivity.Type type;

    @ViewById
    LoginEditText accountEdit;

    @ViewById
    View loginButton;

    InputRequest inputRequest = s -> InputCheck.isEmail(s) || InputCheck.isPhone(s);

    @AfterViews
    void initInputAccountActivity() {
        setTitle(type.getInputAccountTitle());
        ViewStyleUtil.editTextBindButton(loginButton, inputRequest, accountEdit);
    }

    @Click
    void loginButton() {
        String account = accountEdit.getTextString();
        if (InputCheck.isEmail(account)) {
            EmailSetPasswordActivity_.intent(this)
                    .type(type)
                    .account(account)
                    .startForResult(RESULT_SET_PASSWORD);
        } else if (InputCheck.isPhone(account)) {
            PhoneSetPasswordActivity_.intent(this)
                    .type(type)
                    .account(account)
                    .startForResult(RESULT_SET_PASSWORD);
        } else {
            showMiddleToast("输入格式有误");
        }
    }

    @OnActivityResult(RESULT_SET_PASSWORD)
    void onResultSetPassword(int resultCode) {
        if (resultCode == RESULT_OK) {
            setResult(resultCode);
            finish();
        }
    }
}
