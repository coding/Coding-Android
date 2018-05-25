package net.coding.program;

import android.support.v7.app.AlertDialog;

import com.tencent.android.tpush.XGPushManager;

import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.model.EnterpriseDetail;
import net.coding.program.common.model.EnterpriseInfo;
import net.coding.program.project.EnterpriseProjectFragment_;
import net.coding.program.setting.EnterpriseMainSettingFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_enterprise_main)
public class EnterpriseMainActivity extends MainActivity {

    private static final String TAG_ENTERPRSE = "TAG_ENTERPRSE";

    @AfterViews
    void initEnterpriseMainActivity() {
        String host = String.format("%s/team/%s/get", Global.HOST_API, EnterpriseInfo.instance().getGlobalkey());
        getNetwork(host, TAG_ENTERPRSE);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_ENTERPRSE)) {
            if (code == 0) { // 服务过期了
                EnterpriseDetail enterpriseDetail = new EnterpriseDetail(respanse.optJSONObject("data"));
                EnterpriseInfo.instance().update(this, enterpriseDetail);

                if (enterpriseDetail.isLocked()) {
                    String title = "服务已暂停";
                    String message = "您订购的服务已过期，项目、任务等功能操作与高级权限将会失效。如需正常使用，请前往企业版网站订购。";
                    new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton("知道了", null)
                            .show();
                }
            } else {
                // do nothing
            }
        } else {
            super.parseJson(code, respanse, tag, pos, data);
        }
    }

    @Override
    protected void switchProject() {
        switchFragment(EnterpriseProjectFragment_.FragmentBuilder_.class);
    }

    @Override
    protected void switchSetting() {
        switchFragment(EnterpriseMainSettingFragment_.FragmentBuilder_.class);
    }

    protected void startExtraService() {
        // 不启动服务
    }

    @Override
    protected void startPushService() {
        // 私有版没有推送
        if (GlobalData.isPrivateEnterprise()) {
            return;
        }

        runQQPushServer();
    }

    private void runQQPushServer() {
        // 信鸽 push 服务会发 broadcast
//        if (!MyApp.isDebug()) {

//        XGPushConfig.enableDebug(this, true);
        String globalKey = GlobalData.sUserObject.global_key;
        XGPushManager.bindAccount(getApplicationContext(), globalKey);
//        pushInXiaomi();
//        }
    }


}
