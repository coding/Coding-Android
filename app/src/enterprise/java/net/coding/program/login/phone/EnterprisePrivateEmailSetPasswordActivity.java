package net.coding.program.login.phone;

import android.text.TextUtils;
import android.view.View;

import net.coding.program.EnterpriseApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.widget.LoginEditTextNew;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

/**
 * Created by chenchao on 2017/1/4.
 */
@EActivity(R.layout.enterprise_private_activity_email_set_password)
public class EnterprisePrivateEmailSetPasswordActivity extends EmailSetPasswordActivity {

    @Extra
    String enterpriseName = "";

    @ViewById
    LoginEditTextNew privateHost;

    @ViewById
    View enterpriseLine;

    @AfterViews
    void initEnterpriseEmailSetPasswordActivity() {
        privateHost.setText(enterpriseName);
        hideActionbarShade();

        if (TextUtils.isEmpty(enterpriseName)) {
            privateHost.requestFocus();
        } else {
            emailEdit.requestFocus();
        }
    }

    @Override
    protected String getUrl() {
        EnterpriseApp.setPrivateHost(privateHost.getTextString());
        return String.format("%s%s", Global.HOST_API, "/account/password/forget");
    }
}
