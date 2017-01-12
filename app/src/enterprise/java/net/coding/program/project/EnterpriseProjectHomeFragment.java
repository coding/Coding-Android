package net.coding.program.project;

import android.view.View;

import net.coding.program.R;
import net.coding.program.model.EnterpriseInfo;
import net.coding.program.project.detail.ProjectFunction;

import org.androidannotations.annotations.EFragment;

/**
 * Created by chenchao on 2017/1/3.
 */

@EFragment(R.layout.enterprise_fragment_project)
//@OptionsMenu(R.menu.menu_fragment_project_home)
public class EnterpriseProjectHomeFragment extends PrivateProjectHomeFragment {

    @Override
    protected ProjectFunction[] getItems() {
        return new ProjectFunction[]{
                ProjectFunction.dynamic,
                ProjectFunction.task,
                ProjectFunction.document,
                ProjectFunction.code,
                ProjectFunction.member,
                ProjectFunction.readme,
                ProjectFunction.merge
        };
    }

    @Override
    protected void initProjectSettingEntrance(View view) {
        if (EnterpriseInfo.instance().canManagerEnterprise()) { // 所有者可以直接修改项目
            view.findViewById(R.id.projectHeaderLayout).setOnClickListener(clickProjectSetting);
        } else {
            super.initProjectSettingEntrance(view);
        }
    }
}
