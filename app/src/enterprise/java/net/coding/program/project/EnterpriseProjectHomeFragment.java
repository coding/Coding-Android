package net.coding.program.project;

import android.view.View;

import net.coding.program.R;
import net.coding.program.common.model.EnterpriseInfo;
import net.coding.program.project.detail.ProjectFunction;

import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.enterprise_fragment_project)
public class EnterpriseProjectHomeFragment extends PrivateProjectHomeFragment {

    @Override
    protected ProjectFunction[] getItems() {
        return new ProjectFunction[]{
                ProjectFunction.dynamic,
                ProjectFunction.task,
                ProjectFunction.wiki,
                ProjectFunction.document,
                ProjectFunction.code,
                ProjectFunction.member,
                ProjectFunction.branchManage,
                ProjectFunction.releaseManage,
                ProjectFunction.merge,
                ProjectFunction.git
        };
    }

    @Override
    protected void initProjectSettingEntrance(View view) {
        if (EnterpriseInfo.instance().canManagerEnterprise() || mProjectObject.canManagerMember()) { // 所有者可以直接修改项目
            view.findViewById(R.id.projectHeaderLayout).setOnClickListener(clickProjectSetting);
        } else {
            super.initProjectSettingEntrance(view);
        }
    }

}
