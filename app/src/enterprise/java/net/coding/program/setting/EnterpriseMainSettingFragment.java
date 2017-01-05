package net.coding.program.setting;

import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.model.EnterpriseInfo;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by chenchao on 2017/1/5.
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
        int visible = View.GONE;
        if (EnterpriseInfo.instance().isAdmin()) {
            visible = View.VISIBLE;
            companyName.setText(EnterpriseInfo.instance().getName());
        }

        itemManager.setVisibility(visible);
        itemManagerDivide.setVisibility(visible);
    }

    @Click
    void itemManager() {

    }

}
