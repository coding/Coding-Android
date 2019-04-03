package net.coding.program.login.phone;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.login.EmailRegisterActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;

/*
 *  通过手机号重置密码
 */
@EActivity(R.layout.activity_phone_set_password)
public class PhoneSetPasswordActivity extends BackActivity {

    @Extra
    String account;

    PhoneSetPasswordFragment2 fragment;

    @AfterViews
    final void initPhoneSetPasswordActivity() {
        fragment = PhoneSetPasswordFragment2_.builder().account(account).build();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (fragment != null) {
            fragment.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @OnActivityResult(EmailRegisterActivity.RESULT_REGISTER_EMAIL)
    void resultEmailRegister(int result) {
        if (result == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

}
