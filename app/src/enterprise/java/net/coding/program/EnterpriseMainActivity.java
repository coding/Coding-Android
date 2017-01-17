package net.coding.program;

import net.coding.program.project.EnterpriseProjectFragment_;
import net.coding.program.setting.EnterpriseMainSettingFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_enterprise_main)
public class EnterpriseMainActivity extends MainActivity {

    @AfterViews
    void initEnterpriseMainActivity() {
    }

    @Override
    protected void switchProject() {
        switchFragment(EnterpriseProjectFragment_.FragmentBuilder_.class);
    }

    @Override
    protected void switchSetting() {
        switchFragment(EnterpriseMainSettingFragment_.FragmentBuilder_.class);
    }

    protected void startExtraService() {
        // 不启动服务
    }
}
