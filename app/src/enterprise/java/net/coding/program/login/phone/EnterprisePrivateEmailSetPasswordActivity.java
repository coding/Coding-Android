package net.coding.program.login.phone;

import android.view.View;
import android.widget.TextView;

import net.coding.program.EnterpriseApp;
import net.coding.program.R;
import net.coding.program.common.Global;

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

//    @ViewById
//    LoginEditTextNew privateHost;

    @ViewById
    View enterpriseLine;

    @ViewById
    TextView toolbarTitle;

    @AfterViews
    void initEnterpriseEmailSetPasswordActivity() {
        String host = enterpriseName;
        int start = host.indexOf("://");
        if (start != -1) {
            host = host.substring(start + "://".length());
        }

        toolbarTitle.setText(String.format("找回密码\n%s", host));
        hideActionbarShade();

        emailEdit.requestFocus();
    }

    @Override
    protected String getUrl() {
        EnterpriseApp.setPrivateHost(enterpriseName);
        return String.format("%s%s", Global.HOST_API, "/account/password/forget");
    }
}
