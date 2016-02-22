package net.coding.program.login.phone;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;

/*
 *  通过手机号重置密码
 */
@EActivity(R.layout.activity_phone_set_password)
public class PhoneSetPasswordActivity extends BackActivity implements ParentActivity {

    public static final int RESULT_REGISTER_EMAIL = 1;

    public static String REGIST_TIP = "注册 Coding 账号表示您已同意<font color=\"#3bbd79\">《Coding 服务条款》</font>";

    @Extra
    Type type = Type.register;

    @Extra
    String account;

    private RequestParams requestParams = new RequestParams();

    @AfterViews
    final void initPhoneSetPasswordActivity() {
        setTitle(type.getInputAccountTitle());
        Fragment fragment;
        requestParams.put("phone", account);
        fragment = PhoneSetPasswordFragment2_.builder().type(type).account(account).build();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public RequestParams getRequestParmas() {
        return requestParams;
    }

    @Override
    public void next() {
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 2) {
            new AlertDialog.Builder(this)
                    .setTitle("不激活就无法使用 Coding，确定放弃?")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            finish();
        }
    }

    @OnActivityResult(RESULT_REGISTER_EMAIL)
    void resultEmailRegister(int result) {
        if (result == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

}
