package net.coding.program.compatible;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import net.coding.program.common.StartActivity;
import net.coding.program.model.ProjectObject;
import net.coding.program.param.ProjectJumpParam;

import java.util.ArrayList;

/**
 * Created by chenchao on 2016/12/28.
 * 实现同 coding 普通版有差异的地方
 */

public class CodingCompat implements ClassCompatInterface {

    private static ClassCompatInterface substance;

    private CodingCompat() {
    }

    public static void init(ClassCompatInterface substance) {
        CodingCompat.substance = substance;
    }

    public static ClassCompatInterface instance() {
        return substance;
    }

    @Override
    public Class<?> getMainActivity() {
        return substance.getMainActivity();
    }

    @Override
    public Class<?> getMainProjectFragment() {
        return substance.getMainProjectFragment();
    }

    @Override
    public Fragment getProjectHome(ProjectObject projectObject, boolean needReload) {
        return substance.getProjectHome(projectObject, needReload);
    }

    @Override
    public Class getGuideActivity() {
        return substance.getGuideActivity();
    }

    @Override
    public Class getLoginActivity() {
        return substance.getLoginActivity();
    }

    @Override
    public void launchMyDetailActivity(Context context) {
        substance.launchMyDetailActivity(context);
    }

    @Override
    public void launchUserDetailActivity(Context context, String globalKey) {
        substance.launchUserDetailActivity(context, globalKey);
    }

    @Override
    public void launchUserDetailActivity(Context context, String globalKey, int result) {
        substance.launchUserDetailActivity(context, globalKey, result);
    }

    @Override
    public void launchUserDetailActivity(Fragment fragment, String globalKey, int result) {
        substance.launchUserDetailActivity(fragment, globalKey, result);
    }

    @Override
    public void launchAddMemberActivity(Fragment fragment, ProjectObject projectObject, ArrayList<String> pickGlobalKeys, int result) {
        substance.launchAddMemberActivity(fragment, projectObject, pickGlobalKeys, result);
    }

    @Override
    public void launchPickUser(Context context, StartActivity startActivity, int result) {
        substance.launchPickUser(context, startActivity, result);
    }

    @Override
    public void launchProjectMaopao(Fragment fragment, ProjectObject projectObject) {
        substance.launchProjectMaopao(fragment, projectObject);
    }

    @Override
    public void launchPickUser(Activity activity, String relayString) {
        substance.launchPickUser(activity, relayString);
    }

    @Override
    public void closeNotify(Context context, String url) {
        substance.closeNotify(context, url);
    }

    @Override
    public void closePushReceiverActivity(Activity context, String url) {
        substance.closePushReceiverActivity(context, url);
    }

    @Override
    public void launchProjectMaopoaList(Context context, ProjectJumpParam param) {
        substance.launchProjectMaopoaList(context, param);
    }

}

