package net.coding.program.login.auth;

import android.content.Intent;
import android.view.View;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.ViewStyleUtil;
import net.coding.program.common.widget.LoginEditText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by chenchao on 2017/6/27.
 */
@EActivity(R.layout.activity_manually_add_qcode)
public class ManuallyAddQCodeActivity extends BackActivity {

    @ViewById
    View sendButton;

    @ViewById
    LoginEditText name, code;

    @AfterViews
    void initManuallyAddQCodeActivity() {
        ViewStyleUtil.editTextBindButton(sendButton, name, code);
    }

    @Click
    void sendButton() {
        String nameString = name.getTextString();
        String codeString = code.getTextString();
        String url = String.format("otpauth://totp/%s?secret=%s", nameString, codeString);

        if (codeString.length() < 16) {
            showMiddleToast("请输入正确的密钥");
            return;
        }

        Intent intentResult = new Intent();
        intentResult.putExtra("data", url);
        setResult(RESULT_OK, intentResult);
        finish();
    }
}
