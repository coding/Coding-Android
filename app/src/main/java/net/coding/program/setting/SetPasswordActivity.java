package net.coding.program.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.BaseActivity;
import net.coding.program.Global;
import net.coding.program.LoginActivity_;
import net.coding.program.R;
import net.coding.program.common.CustomDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

@EActivity(R.layout.activity_set_password)
@OptionsMenu(R.menu.set_password)
public class SetPasswordActivity extends BaseActivity {

    @ViewById
    TextView oldPassword;

    @ViewById
    TextView newPassword;

    @ViewById
    TextView confirmPassword;

    @AfterViews
    void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OptionsItem(android.R.id.home)
    void back() {
        onBackPressed();
    }

    final String Url = Global.HOST + "/api/user/updatePassword";

    @OptionsItem
    void submit() {
        RequestParams params = new RequestParams();
        try {
            params.put("current_password", Global.sha1(oldPassword.getText().toString()));
            params.put("password", Global.sha1(newPassword.getText().toString()));
            params.put("confirm_password", Global.sha1(confirmPassword.getText().toString()));
            postNetwork(Url, params, "");
        } catch (Exception e) {
            showMiddleToast(e.toString());
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (code == 0) {
            showButtomToast("密码修改成功");
            popDialog();
        } else {
            showErrorMsg(code, respanse);
        }
    }

    private void popDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("修改密码后需要重新登录")
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        startActivity(new Intent(SetPasswordActivity.this, LoginActivity_.class));
                    }
                })
                .setCancelable(false)
                .show();

        CustomDialog.dialogTitleLineColor(this, dialog);
    }

}
