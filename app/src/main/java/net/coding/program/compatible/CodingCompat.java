package net.coding.program.compatible;

import android.content.Context;
import android.support.v4.app.Fragment;

import net.coding.program.model.ProjectObject;

/**
 * Created by chenchao on 2016/12/28.
 * 实现同 coding 普通版有差异的地方
 */

public class CodingCompat implements ClassCompatInterface {

    private static ClassCompatInterface substance;

    private CodingCompat() {}

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
}

