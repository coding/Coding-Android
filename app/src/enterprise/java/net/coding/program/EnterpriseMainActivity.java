package net.coding.program;

import android.support.v7.app.AlertDialog;

import net.coding.program.common.Global;
import net.coding.program.model.EnterpriseDetail;
import net.coding.program.model.EnterpriseInfo;
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
}
