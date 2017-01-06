package net.coding.program.compatible;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import net.coding.program.EnterpriseMainActivity_;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.EnterpriseProjectFragment_;
import net.coding.program.project.EnterpriseProjectHomeFragment_;
import net.coding.program.user.EnterpriseAddMemberActivity_;
import net.coding.program.user.EnterpriseLoginActivity_;
import net.coding.program.user.EnterpriseMyDetailActivity_;
import net.coding.program.user.EnterpriseUserDetailActivity_;

import java.util.ArrayList;

/**
 * Created by chenchao on 2016/12/28.
 */

public class EnterpriseCompatImp implements ClassCompatInterface {

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
        return EnterpriseLoginActivity_.class;
    }

    @Override
    public Class getLoginActivity() {
        return EnterpriseLoginActivity_.class;
    }

    @Override
    public void launchMyDetailActivity(Context context) {
        EnterpriseMyDetailActivity_.intent(context).start();
    }

    @Override
    public void launchUserDetailActivity(Context context, String globalKey) {
        EnterpriseUserDetailActivity_.intent(context).globalKey(globalKey).start();
    }

    @Override
    public void launchUserDetailActivity(Context context, String globalKey, int result) {
        EnterpriseUserDetailActivity_.intent(context).globalKey(globalKey).startForResult(result);
    }

    @Override
    public void launchUserDetailActivity(Fragment fragment, String globalKey, int result) {
        EnterpriseUserDetailActivity_.intent(fragment).globalKey(globalKey).startForResult(result);
    }

    @Override
    public void launchAddMemberActivity(Fragment fragment, ProjectObject projectObject, ArrayList<String> pickGlobalKeys, int result) {
        Intent intent = new Intent(fragment.getActivity(), EnterpriseAddMemberActivity_.class);
        intent.putExtra("projectObject", projectObject);
        intent.putExtra("pickedGlobalKeys", pickGlobalKeys);
        fragment.startActivityForResult(intent, result);
    }
}
