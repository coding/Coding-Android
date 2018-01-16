package net.coding.program.login.phone;

import android.view.View;

import net.coding.program.EnterpriseApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.util.ViewStyleUtil;
import net.coding.program.common.widget.LoginEditText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

/**
 * Created by chenchao on 2017/1/4.
 */
@EActivity(R.layout.enterprise_activity_email_set_password)
public class EnterpriseEmailSetPasswordActivity extends EmailSetPasswordActivity {

    @Extra
    String enterpriseName = "";

    @ViewById
    LoginEditText enterpriseEdit;

    @ViewById
    View enterpriseLine;

    @AfterViews
    void initEnterpriseEmailSetPasswordActivity() {
        enterpriseEdit.setText(enterpriseName);
        hideActionbarShade();
        enterpriseEdit.setOnEditFocusChange(createEditLineFocus(enterpriseLine));
    }

    @Override
    protected String getUrl() {
        EnterpriseApp.setHost(enterpriseEdit.getTextString());
        return String.format("%s%s", Global.HOST_API, "/account/password/forget");
    }

    protected void initViewStyle() {
        ViewStyleUtil.editTextBindButton(loginButton, emailEdit, captchaEdit, enterpriseEdit);
    }
}
