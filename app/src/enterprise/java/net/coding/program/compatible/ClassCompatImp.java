package net.coding.program.compatible;

import android.support.v4.app.Fragment;

import net.coding.program.EnterpriseMainActivity_;
import net.coding.program.LoginActivity_;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.EnterpriseProjectFragment_;
import net.coding.program.project.EnterpriseProjectHomeFragment_;

/**
 * Created by chenchao on 2016/12/28.
 */

public class ClassCompatImp implements ClassCompatInterface {

    @Override
    public Class<?> getMainActivity() {
        return EnterpriseMainActivity_.class;
    }

    @Override
    public Class<?> getMainProjectFragment() {
        return EnterpriseProjectFragment_.FragmentBuilder_.class;
    }

    @Override
    public Fragment getProjectHome(ProjectObject projectObject, boolean needReload) {
        return EnterpriseProjectHomeFragment_.builder()
                .mProjectObject(projectObject)
                .needReload(needReload)
                .build();
    }

    @Override
    public Class getGuideActivity() {
        return LoginActivity_.class;
    }

    @Override
    public Class getLoginActivity() {
        return LoginActivity_.class;
    }
}
