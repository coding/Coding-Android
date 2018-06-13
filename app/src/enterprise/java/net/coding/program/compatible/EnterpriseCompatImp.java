package net.coding.program.compatible;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.tencent.android.tpush.XGPushManager;

import net.coding.program.EnterpriseMainActivity_;
import net.coding.program.EnterpriseMyPushReceiver;
import net.coding.program.UserDetailEditActivity_;
import net.coding.program.common.GlobalData;
import net.coding.program.common.StartActivity;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.ProjectObject;
import net.coding.program.guide.EnterpriseGuideActivity_;
import net.coding.program.login.phone.EnterprisePrivateEmailSetPasswordActivity_;
import net.coding.program.param.ProjectJumpParam;
import net.coding.program.project.EnterpriseProjectHomeFragment_;
import net.coding.program.project.detail.TopicEditFragment;
import net.coding.program.project.maopao.EnterprisePrivateProjectMaopaoEditFragment_;
import net.coding.program.project.maopao.EnterpriseProjectMaopaoActivity_;
import net.coding.program.project.maopao.EnterpriseProjectMaopaoEditFragment_;
import net.coding.program.user.EnterpriseAddMemberActivity_;
import net.coding.program.user.EnterpriseLoginActivity_;
import net.coding.program.user.PickUserActivity_;
import net.coding.program.user.PickUserRelayMessageActivity_;
import net.coding.program.user.UserDetailMoreActivity_;

import java.util.ArrayList;

/**
 * Created by chenchao on 2016/12/28.
 * 企业版与平台板不同的跳转页面
 * 私有部署的文件相关部分还是使用个人版 api
 */

public class EnterpriseCompatImp implements ClassCompatInterface {

    @Override
    public TopicEditFragment getProjectMaopaoEditFragment() {
        if (GlobalData.isPrivateEnterprise()) {
            return EnterprisePrivateProjectMaopaoEditFragment_.builder().build();
        } else {
            return EnterpriseProjectMaopaoEditFragment_.builder().build();
        }
    }

    @Override
    public void loginOut(Context context, String gk) {
        XGPushManager.delAccount(context, gk);
    }

    @Override
    public String getFileAuthor() {
        return "net.coding.program.enterprise.fileprovider";
    }

    @Override
    public Class<?> getMainActivity() {
        return EnterpriseMainActivity_.class;
    }

    @Override
    public Fragment getProjectHome(ProjectObject projectObject, boolean needReload) {
        return EnterpriseProjectHomeFragment_.builder()
                .mProjectObject(projectObject)
                .needReload(needReload)
                .build();
    }

    @Override
    public void launchEnterprisePrivateEmailSetPasswordActivity(Context context) {
        EnterprisePrivateEmailSetPasswordActivity_.intent(context)
                .enterpriseName(AccountInfo.loadLastPrivateHost(context))
                .start();
    }


    @Override
    public Class getGuideActivity() {
        return EnterpriseGuideActivity_.class;
    }

    @Override
    public Class getLoginActivity() {
        return EnterpriseLoginActivity_.class;
    }

    @Override
    public void launchMyDetailActivity(Context context) {
        UserDetailEditActivity_.intent(context).start();
    }

    @Override
    public void launchUserDetailActivity(Context context, String globalKey) {
        UserDetailMoreActivity_.intent(context).globalKey(globalKey).start();
    }

    @Override
    public void launchUserDetailActivity(Context context, String globalKey, int result) {
        UserDetailMoreActivity_.intent(context).globalKey(globalKey).start();
    }

    @Override
    public void launchUserDetailActivity(Fragment fragment, String globalKey, int result) {
        UserDetailMoreActivity_.intent(fragment).globalKey(globalKey).startForResult(result);
    }

    @Override
    public void launchAddMemberActivity(Fragment fragment, ProjectObject projectObject, ArrayList<String> pickGlobalKeys, int result) {
        Intent intent = new Intent(fragment.getActivity(), EnterpriseAddMemberActivity_.class);
        intent.putExtra("projectObject", projectObject);
        intent.putExtra("pickedGlobalKeys", pickGlobalKeys);
        fragment.startActivityForResult(intent, result);
    }

    @Override
    public void launchPickUser(Context context, StartActivity startActivity, int result) {
        Intent intent = new Intent(context, PickUserActivity_.class);
        startActivity.startActivityForResult(intent, result);
    }

    @Override
    public void launchProjectMaopao(Fragment fragment, ProjectObject projectObject) {
        EnterpriseProjectMaopaoActivity_.intent(fragment)
                .projectObject(projectObject)
                .start();
    }

    @Override
    public void launchProjectMaopao(Activity fragment, ProjectObject projectObject) {
        EnterpriseProjectMaopaoActivity_.intent(fragment)
                .projectObject(projectObject)
                .start();
    }

    @Override
    public void launchPickUser(Activity activity, String relayString) {
        PickUserRelayMessageActivity_.intent(activity)
                .relayString(relayString)
                .start();
    }

    @Override
    public void closeNotify(Context context, String url) {
        EnterpriseMyPushReceiver.closeNotify(context, url);
    }

    @Override
    public void closePushReceiverActivity(Activity context, String url) {
        Intent resultIntent = new Intent(EnterpriseMyPushReceiver.PushClickBroadcast);
        resultIntent.setPackage(context.getPackageName());
        resultIntent.putExtra("data", url);
        context.sendBroadcast(resultIntent);
        context.finish();
    }

    @Override
    public void launchProjectMaopoaList(Context context, ProjectJumpParam param) {
        EnterpriseProjectMaopaoActivity_.intent(context)
                .jumpParam(param)
                .start();
    }

    @Override
    public void launchSetGKActivity(Context context) {
        // 跳转到设置 GK 界面，企业版没有这个界面
    }

}
