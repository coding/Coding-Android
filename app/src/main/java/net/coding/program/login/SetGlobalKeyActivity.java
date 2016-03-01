package net.coding.program.login;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.loopj.android.http.RequestParams;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.ViewStyleUtil;
import net.coding.program.common.widget.LoginEditText;
import net.coding.program.model.AccountInfo;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;


@EActivity(R.layout.activity_set_global_key)
public class SetGlobalKeyActivity extends BackActivity {

    private static boolean isShowing = false;

    public static boolean isShowing() {
        return isShowing;
    }

    @ViewById
    LoginEditText globalKeyEdit;

    @ViewById
    View loginButton;

    @AfterViews
    final void initPhoneSetPasswordActivity() {
        ViewStyleUtil.editTextBindButton(loginButton, globalKeyEdit);
    }

    @Click
    void loginButton() {
        String globalKeyString = globalKeyEdit.getText().toString();

        if (globalKeyString.length() < 3) {
            showMiddleToast("用户名（个性后缀）至少为3个字符");
            return;
        }

        String url = Global.HOST_API + "/account/global_key/activate";
        RequestParams params = new RequestParams();
        params.put("global_key", globalKeyString);

        MyAsyncHttpClient.post(SetGlobalKeyActivity.this, url, params, new MyJsonResponse(SetGlobalKeyActivity.this) {
            @Override
            public void onMySuccess(JSONObject respanse) {
                super.onMySuccess(respanse);

                MyApp.sUserObject.global_key = globalKeyString;
                AccountInfo.saveReloginInfo(SetGlobalKeyActivity.this, MyApp.sUserObject);

                SetGlobalKeyActivity.this.setResult(Activity.RESULT_OK);
                SetGlobalKeyActivity.this.finish();
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);

                showProgressBar(false, "");
            }
        });

        showProgressBar(true, "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isShowing = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isShowing = false;
    }
}
