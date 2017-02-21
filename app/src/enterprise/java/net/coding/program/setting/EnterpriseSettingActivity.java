package net.coding.program.setting;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_enterprise_setting)
public class EnterpriseSettingActivity extends BackActivity {

    @AfterViews
    void initView(){
        setActionBarTitle(getString(R.string.enterprise_setting));
    }

    @Click
    void enterpriseName(){
        EnterpriseNameActivity_.intent(this).start();
    }
}
