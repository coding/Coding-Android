package net.coding.program.login;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.umeng.UmengEvent;

import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_reset_password_base)
public class ResetPasswordActivity extends ResetPasswordBaseActivity {

    @Override
    String getRequestHost() {
        umengEvent(UmengEvent.USER, "重置密码");
        return Global.HOST_API + "/resetPassword";
    }
}
