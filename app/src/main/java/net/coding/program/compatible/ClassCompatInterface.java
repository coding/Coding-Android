package net.coding.program.compatible;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import net.coding.program.common.StartActivity;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.project.detail.TopicEditFragment;

import java.util.ArrayList;

/**
 * Created by chenchao on 2016/12/28.
 * 用于抽象出 普通版 和 企业编 不同的地方
 */

public interface ClassCompatInterface {

    Class<?> getMainActivity();

    TopicEditFragment getProjectMaopaoEditFragment();

    Fragment getProjectHome(ProjectObject projectObject, boolean needRelaod);

    Class getGuideActivity();

    Class getLoginActivity();

    void loginOut(Context context, String gk);

    void launchMyDetailActivity(Context context);

    void launchUserDetailActivity(Context context, String globalKey);

    void launchUserDetailActivity(Context context, String globalKey, int result);

    void launchUserDetailActivity(Fragment fragment, String globalKey, int result);

    void launchAddMemberActivity(Fragment fragment, ProjectObject projectObject, ArrayList<String> pickGlobalKeys, int result);

    void launchPickUser(Context context, StartActivity startActivity, int result);

    void launchProjectMaopao(Fragment fragment, ProjectObject projectObject);

    void launchProjectMaopao(Activity fragment, ProjectObject projectObject);

    void launchPickUser(Activity activity, String relayString);

    void closeNotify(Context context, String url);

    void closePushReceiverActivity(Activity context, String url);

    void launchProjectMaopoaList(Context context, ProjectJumpParam param);

    void launchSetGKActivity(Context context);

    void launchEnterprisePrivateEmailSetPasswordActivity(Context context);

    String getFileAuthor();

}
