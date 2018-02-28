package net.coding.program.compatible;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import net.coding.program.LoginActivity_;
import net.coding.program.MainActivity_;
import net.coding.program.common.StartActivity;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.guide.GuideActivity;
import net.coding.program.login.SetGlobalKeyActivity_;
import net.coding.program.network.constant.Friend;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.project.PrivateProjectHomeFragment_;
import net.coding.program.project.PublicProjectHomeFragment_;
import net.coding.program.project.maopao.ProjectMaopaoActivity_;
import net.coding.program.user.AddFollowActivity_;
import net.coding.program.user.MyDetailActivity_;
import net.coding.program.user.UserDetailActivity_;
import net.coding.program.user.UsersListActivity_;

import java.util.ArrayList;

/**
 * Created by chenchao on 2016/12/29.
 * coding
 */

public class CodingCompatImp implements ClassCompatInterface {

    @Override
    public Class<?> getMainActivity() {
        return MainActivity_.class;
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

    @Override
    public void launchAddMemberActivity(Fragment fragment, ProjectObject projectObject, ArrayList<String> pickGlobalKeys, int result) {
        Intent intent = new Intent(fragment.getActivity(), AddFollowActivity_.class);
        intent.putExtra("mProjectObject", projectObject);
        fragment.startActivityForResult(intent, result);
    }

    @Override
    public void launchPickUser(Context context, StartActivity startActivity, int result) {
        Intent intent;
        intent = new Intent(context, UsersListActivity_.class);
        intent.putExtra("type", Friend.Follow);
        intent.putExtra("selectType", true);
        intent.putExtra("hideFollowButton", true);
        startActivity.startActivityForResult(intent, result);
    }


    @Override
    public void launchProjectMaopao(Fragment fragment, ProjectObject projectObject) {
        ProjectMaopaoActivity_.intent(fragment)
                .projectObject(projectObject)
                .start();
    }

    @Override
    public void launchPickUser(Activity activity, String relayString) {
        UsersListActivity_.intent(activity)
                .type(Friend.Follow)
                .hideFollowButton(true)
                .relayString(relayString)
                .start();
    }

    @Override
    public void closeNotify(Context context, String url) {
        // TODO: 2017/11/9  
//        CodingMyPushReceiver.closeNotify(context, url);
    }

    @Override
    public void closePushReceiverActivity(Activity context, String url) {
        // TODO: 2017/11/9  
//        Intent resultIntent = new Intent(CodingMyPushReceiver.PushClickBroadcast);
//        resultIntent.setPackage(context.getPackageName());
//        resultIntent.putExtra("data", url);
//        context.sendBroadcast(resultIntent);
//        context.finish();
    }

    @Override
    public void launchProjectMaopoaList(Context context, ProjectJumpParam param) {
        // coding 可以直接跳转到详情，不需要实现这个
    }

    @Override
    public void launchSetGKActivity(Context context) {
        Intent activityIntent = new Intent(context, SetGlobalKeyActivity_.class);
        context.startActivity(activityIntent);
    }
}
