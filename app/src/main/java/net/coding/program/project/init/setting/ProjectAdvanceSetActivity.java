package net.coding.program.project.init.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.coding.program.BaseActivity;
import net.coding.program.LoginActivity_;
import net.coding.program.R;
import net.coding.program.common.CustomDialog;
import net.coding.program.common.Global;
import net.coding.program.common.SimpleSHA1;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.ProjectObject;
import net.coding.program.project.init.InitProUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by jack wang on 2015/3/31.
 */
@EActivity(R.layout.init_activity_project_advance_set)
public class ProjectAdvanceSetActivity extends BaseActivity {

    private static final String TAG = "ProjectAdvanceSetActivity";

    final String host = Global.HOST + "/api/project/";
    String hostDelete;

    ProjectObject mProjectObject;

    @ViewById
    Button deleteBut;

    @AfterViews
    protected final void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProjectObject = (ProjectObject) getIntent().getSerializableExtra("projectObject");
    }

    @Click
    void deleteBut() {
        showDeleteDialog();
    }

    void showDeleteDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.init_dialog_text_entry, null);
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


    @OptionsItem(android.R.id.home)
    protected final void back() {
        finish();
    }

}
