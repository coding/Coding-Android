package net.coding.program;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;

import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.UnreadNotify;
import net.coding.program.common.WeakRefHander;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.login.ResetPasswordActivity_;
import net.coding.program.login.UserActiveActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cc191954 on 14-8-14.
 * 启动页面
 */
@EActivity(R.layout.activity_enterprise_entrance)
public class EnterpriseEntranceActivity extends BaseActivity implements Handler.Callback {

    public final String HOST_CURRENT = getHostCurrent();

    public static String getHostCurrent() {
        return Global.HOST_API + "/current_user";
    }

    private static final int HANDLER_MESSAGE_NEXT_ACTIVITY = 1;

    Uri background = null;
    boolean mNeedUpdateUser = false;
    WeakRefHander mWeakRefHandler;

    @AfterViews
    void init() {
        Uri uriData = getIntent().getData();
        if (uriData != null) {
            String url = uriData.toString();
            String path = uriData.getPath();
            switch (path) {
                case "/app/detect": {
                    String link = Global.decodeUtf8(uriData.getQueryParameter("link"));
                    Uri uriLink = Uri.parse(link);
                    String linkPath = uriLink.getPath();

                    switch (linkPath) {
                        case "/activate":
                            UserActiveActivity_.intent(this)
                                    .link(link)
                                    .start();
                            break;
                        case "/user/resetPassword":
                            ResetPasswordActivity_.intent(this)
                                    .link(link)
                                    .start();
                            break;
                        default:
                            WebActivity_.intent(this)
                                    .url(link)
                                    .start();
                            break;
                    }
                    break;
                }

                default: {
                    MyApp.openNewActivityFromMain(this, url);
                }
            }

            finish();
            return;
        }

        mWeakRefHandler = new WeakRefHander(this);

        if (!mNeedUpdateUser) {
            mWeakRefHandler.start(HANDLER_MESSAGE_NEXT_ACTIVITY, 1200);
        }

        if (AccountInfo.isLogin(this)) {
            getNetwork(HOST_CURRENT, HOST_CURRENT);
            mNeedUpdateUser = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        XGPushClickedResult result = XGPushManager.onActivityStarted(this);
        if (result != null) {
            String custom = result.getCustomContent();
            if (custom != null && !custom.isEmpty()) {
                try {
                    JSONObject json = new JSONObject(custom);
                    String url = json.getString("param_url");
                    CodingCompat.instance().closePushReceiverActivity(EnterpriseEntranceActivity.this, url);

                } catch (Exception e) {
                    Global.errorLog(e);
                }
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == HANDLER_MESSAGE_NEXT_ACTIVITY) {
            next();
        }
        return true;
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_CURRENT)) {
            mNeedUpdateUser = false;
            if (code == 0) {
                UserObject user = new UserObject(respanse.getJSONObject("data"));
                AccountInfo.saveAccount(this, user);
                GlobalData.sUserObject = user;
                AccountInfo.saveReloginInfo(this, user);
                next();
            } else {
                showDialog("更新",
                        "刷新账户信息失败",
                        (dialog, which) -> getNetwork(HOST_CURRENT, HOST_CURRENT),
                        (dialog, which) -> finish(),
                        "重试",
                        "关闭程序");
            }
        }
    }

    void next() {
        Intent intent;
        String mGlobalKey = AccountInfo.loadAccount(this).global_key;
        if (mGlobalKey.isEmpty()) {
            intent = new Intent(this, CodingCompat.instance().getGuideActivity());
            if (background != null) {
                intent.putExtra(LoginActivity.EXTRA_BACKGROUND, background);
            }

        } else {
//            if (AccountInfo.needDisplayGuide(this)) {
//                intent = new Intent(this, FeatureActivity_.class);
//            } else {
            intent = new Intent(this, CodingCompat.instance().getMainActivity());
//            }
        }

        startActivity(intent);

        UnreadNotify.update(this);
        finish();

        overridePendingTransition(R.anim.fade_in, R.anim.alpha_out);
    }
}

