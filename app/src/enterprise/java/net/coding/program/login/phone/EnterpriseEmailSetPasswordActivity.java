package net.coding.program.login.phone;

import net.coding.program.R;
import net.coding.program.common.widget.LoginEditText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

/**
 * Created by chenchao on 2017/1/4.
 *
 */
@EActivity(R.layout.enterprise_activity_email_set_password)
public class EnterpriseEmailSetPasswordActivity extends EmailSetPasswordActivity {

    @Extra
    String enterpriseName = "";

    @ViewById
    LoginEditText enterpriseEdit;

    @AfterViews
    void initEnterpriseEmailSetPasswordActivity() {
        enterpriseEdit.setText(enterpriseName);
    }
}
