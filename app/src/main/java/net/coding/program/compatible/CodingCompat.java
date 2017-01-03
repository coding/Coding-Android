package net.coding.program.compatible;

import android.support.v4.app.Fragment;

import net.coding.program.model.ProjectObject;

/**
 * Created by chenchao on 2016/12/28.
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
}
