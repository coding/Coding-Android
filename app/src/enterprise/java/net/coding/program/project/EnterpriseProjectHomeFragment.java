package net.coding.program.project;

import net.coding.program.R;
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
}
