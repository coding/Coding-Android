package net.coding.program.compatible;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import net.coding.program.CodingMyPushReceiver;
import net.coding.program.LoginActivity_;
import net.coding.program.MainActivity_;
import net.coding.program.common.StartActivity;
import net.coding.program.common.guide.GuideActivity;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.MainProjectFragment_;
import net.coding.program.project.PrivateProjectHomeFragment_;
import net.coding.program.project.PublicProjectHomeFragment_;
import net.coding.program.project.maopao.ProjectMaopaoActivity_;
import net.coding.program.user.AddFollowActivity_;
import net.coding.program.user.MyDetailActivity_;
import net.coding.program.user.UserDetailActivity_;
import net.coding.program.user.UsersListActivity;
import net.coding.program.user.UsersListActivity_;

import java.util.ArrayList;

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
        intent.putExtra("type", UsersListActivity.Friend.Follow);
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
                .type(UsersListActivity.Friend.Follow)
                .hideFollowButton(true)
                .relayString(relayString)
                .start();
    }

    @Override
    public void closeNotify(Context context, String url) {
        CodingMyPushReceiver.closeNotify(context, url);
    }

    @Override
    public void closePushReceiverActivity(Activity context, String url) {
        Intent resultIntent = new Intent(CodingMyPushReceiver.PushClickBroadcast);
        resultIntent.setPackage(context.getPackageName());
        resultIntent.putExtra("data", url);
        context.sendBroadcast(resultIntent);
        context.finish();
    }
}
