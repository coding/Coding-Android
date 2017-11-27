package net.coding.program.setting;

import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.model.EnterpriseInfo;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by chenchao on 2017/1/5.
 * 企业版的设置主界面
 */
@EFragment(R.layout.enterprise_fragment_main_setting)
public class EnterpriseMainSettingFragment extends MainSettingFragment {

    @ViewById
    View itemManagerDivide;

    @ViewById
    View itemManager;

    @ViewById
    TextView companyName;

    @AfterViews
    void initEnterpriseMainSettingFragment() {
        setToolbar("我的", R.id.toolbar);
        int visible = View.GONE;
        if (EnterpriseInfo.instance().canManagerEnterprise()) {
            visible = View.VISIBLE;
            companyName.setText(EnterpriseInfo.instance().getName());
        }

        itemManager.setVisibility(visible);
        itemManagerDivide.setVisibility(visible);

        setHasOptionsMenu(false);
    }

    @Click
    void itemManager() {
        EnterpriseAccountActivity_.intent(this).start();
    }

}
