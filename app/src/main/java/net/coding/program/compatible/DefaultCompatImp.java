package net.coding.program.compatible;

import android.content.Context;
import android.support.v4.app.Fragment;

import net.coding.program.LoginActivity_;
import net.coding.program.MainActivity_;
import net.coding.program.common.guide.GuideActivity;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.MainProjectFragment_;
import net.coding.program.project.PrivateProjectHomeFragment_;
import net.coding.program.project.PublicProjectHomeFragment_;
import net.coding.program.user.MyDetailActivity_;
import net.coding.program.user.UserDetailActivity_;

/**
 * Created by chenchao on 2016/12/29.
 * coding
 */

public class DefaultCompatImp implements ClassCompatInterface {

    @Override
    public Class<?> getMainActivity() {
        return MainActivity_.class;
    }

    @Override
    public Class<?> getMainProjectFragment() {
        return MainProjectFragment_.FragmentBuilder_.class;
    }

    @Override
    public Fragment getProjectHome(ProjectObject projectObject, boolean needReload) {
        if (projectObject.isPublic()) {
            return PublicProjectHomeFragment_.builder()
                    .mProjectObject(projectObject)
                    .needReload(needReload)
                    .build();
        } else {
            return PrivateProjectHomeFragment_.builder()
                    .mProjectObject(projectObject)
                    .needReload(needReload)
                    .build();
        }
    }

    @Override
    public Class getGuideActivity() {
        return GuideActivity.class;
    }

    @Override
    public Class getLoginActivity() {
        return LoginActivity_.class;
    }

    @Override
    public void launchMyDetailActivity(Context context) {
        MyDetailActivity_.intent(context).start();
    }

    @Override
    public void launchUserDetailActivity(Context context, String globalKey) {
        UserDetailActivity_.intent(context).globalKey(globalKey).start();
    }

    @Override
    public void launchUserDetailActivity(Context context, String globalKey, int result) {
        UserDetailActivity_.intent(context).globalKey(globalKey).startForResult(result);
    }

    @Override
    public void launchUserDetailActivity(Fragment fragment, String globalKey, int result) {
        UserDetailActivity_.intent(fragment).globalKey(globalKey).startForResult(result);
    }
}
