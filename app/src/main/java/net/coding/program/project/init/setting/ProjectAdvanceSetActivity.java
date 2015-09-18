package net.coding.program.project.init.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.coding.program.BackActivity;
import net.coding.program.LoginActivity_;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.CustomDialog;
import net.coding.program.common.Global;
import net.coding.program.common.SimpleSHA1;
import net.coding.program.common.WeakRefHander;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.common.umeng.UmengEvent;
import net.coding.program.login.auth.AuthInfo;
import net.coding.program.login.auth.TotpClock;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.init.InitProUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jack wang on 2015/3/31.
 * 删除项目
 */
@EActivity(R.layout.init_activity_project_advance_set)
public class ProjectAdvanceSetActivity extends BackActivity implements Handler.Callback {

    private static final String TAG = "ProjectAdvanceSetActivity";

    private final String host = Global.HOST_API + "/project/";
    private final String TAG_DELETE_PROJECT_2FA = "TAG_DELETE_PROJECT_2FA";
    private final String HOST_NEED_2FA = Global.HOST_API + "/user/2fa/method";
    @ViewById
    Button deleteBut;
    Handler hander2fa;
    private String hostDelete;
    private ProjectObject mProjectObject;
    private EditText edit2fa;

    @AfterViews
    protected final void initProjectAdvanceSetActivity() {
        mProjectObject = (ProjectObject) getIntent().getSerializableExtra("projectObject");
        hander2fa = new WeakRefHander(this, 100);
    }

    @Click
    void deleteBut() {
        showProgressBar(true);
        getNetwork(HOST_NEED_2FA, HOST_NEED_2FA);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (edit2fa != null) {
            String secret = AccountInfo.loadAuth(this, MyApp.sUserObject.global_key);
            if (secret.isEmpty()) {
                return true;
            }

            String code2FA = new AuthInfo(secret, new TotpClock(this)).getCode();
            edit2fa.setText(code2FA);
        }
        return true;
    }

    private void showDeleteDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.dialog_delete_project, null);
        final EditText edit1 = (EditText) textEntryView.findViewById(R.id.edit1);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder
                .setTitle("需要验证密码")
                .setView(textEntryView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String editStr1 = edit1.getText().toString().trim();
                        if (TextUtils.isEmpty(editStr1)) {
                            Toast.makeText(ProjectAdvanceSetActivity.this, "密码不能为空", Toast.LENGTH_LONG).show();
                            return;
                        }
                        action_delete(editStr1);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).show();
        CustomDialog.dialogTitleLineColor(this, dialog);
    }

    private void showDeleteDialog2fa() {
        hander2fa.sendEmptyMessage(0);
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.dialog_delete_project_2fa, null);
        edit2fa = (EditText) textEntryView.findViewById(R.id.edit1);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder
                .setTitle("需要验证码")
                .setView(textEntryView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String editStr1 = edit2fa.getText().toString().trim();
                        if (TextUtils.isEmpty(editStr1)) {
                            Toast.makeText(ProjectAdvanceSetActivity.this, "密码不能为空", Toast.LENGTH_LONG).show();
                            return;
                        }
                        actionDelete2FA(editStr1);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();

        CustomDialog.dialogTitleLineColor(this, dialog);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                hander2fa.removeMessages(0);
                edit2fa = null;
            }
        });
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(HOST_NEED_2FA)) {
            showProgressBar(false);
            if (code == 0) {
                String passwordType = respanse.optString("data", "");
                if (passwordType.equals("totp")) {
                    showDeleteDialog2fa();
                } else { //  password
                    showDeleteDialog();
                }
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_DELETE_PROJECT_2FA)) {
            showProgressBar(false);
            if (code == 0) {
                umengEvent(UmengEvent.PROJECT, "删除项目2fa");
                showButtomToast("删除成功");
                InitProUtils.intentToMain(ProjectAdvanceSetActivity.this);
                finish();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    private void actionDelete2FA(String code) {
        showProgressBar(true);
        deleteNetwork(mProjectObject.getHttpDeleteProject2fa(code), TAG_DELETE_PROJECT_2FA);
    }

    void action_delete(String pwd) {
        hostDelete = host + mProjectObject.getId();
        RequestParams params = new RequestParams();
        params.put("user_name", AccountInfo.loadAccount(this).name);
        params.put("name", mProjectObject.name);
        params.put("project_id", "" + mProjectObject.getId());
        try {
            params.put("password", SimpleSHA1.sha1(pwd));
        } catch (Exception e) {
            e.printStackTrace();
        }
        AsyncHttpClient client = MyAsyncHttpClient.createClient(this);
        client.delete(this, hostDelete, null, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                showProgressBar(false);
                try {
                    int code = response.getInt("code");

                    if (code == 1000) {
                        startActivity(new Intent(ProjectAdvanceSetActivity.this, LoginActivity_.class));
                    }
                    if (code == 0) {
                        umengEvent(UmengEvent.PROJECT, "删除项目");
                        showButtomToast("删除成功");
                        InitProUtils.intentToMain(ProjectAdvanceSetActivity.this);
                        finish();
                    } else {
                        showErrorMsg(code, response);
                    }

                } catch (Exception e) {
                    Global.errorLog(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showProgressBar(false);
                try {
                    showErrorMsg(NetworkImpl.NETWORK_ERROR, errorResponse);
                } catch (Exception e) {
                    Global.errorLog(e);
                }
            }

            @Override
            public void onStart() {
                showProgressBar(true, "正在删除项目...");
            }
        });
    }
}
