package net.coding.program.setting;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_enterprise_name)
public class EnterpriseNameActivity extends BackActivity {

    @AfterViews
    void initView(){
        setActionBarTitle("企业名称");
    }
}
