package net.coding.program.compatible;

import android.support.v4.app.Fragment;

import net.coding.program.model.ProjectObject;

/**
 * Created by chenchao on 2016/12/28.
 * 用于抽象出 普通版 和 企业编 不同的地方
 */

public interface ClassCompatInterface {

    Class<?> getMainActivity();

    Class<?> getMainProjectFragment();

    Fragment getProjectHome(ProjectObject projectObject, boolean needRelaod);

    Class getGuideActivity();

    Class getLoginActivity();
}
