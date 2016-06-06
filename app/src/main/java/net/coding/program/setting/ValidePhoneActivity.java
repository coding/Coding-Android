package net.coding.program.setting;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.WeakRefHander;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.ViewStyleUtil;
import net.coding.program.common.widget.LoginEditText;
import net.coding.program.common.widget.ValidePhoneView;
import net.coding.program.login.auth.AuthInfo;
import net.coding.program.login.auth.TotpClock;
import net.coding.program.login.phone.CountryPickActivity_;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.PhoneCountry;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chenchao on 15/12/28.
 */

@EActivity(R.layout.activity_valide_phone)
public class ValidePhoneActivity extends BackActivity {

    private static final int RESULT_PICK_COUNTRY = 10;

    private static final String TAG_SET_USER_INFO = "TAG_SET_USER_INFO";

    @ViewById
    LoginEditText editPhone, editCode, passwordEdit, twoFAEdit;

    @ViewById
    TextView loginButton, countryCode;

    @ViewById
    ValidePhoneView sendPhoneMessage;

    UserObject user;

    Handler handler2FA;

    PhoneCountry pickCountry = PhoneCountry.getChina();

    @AfterViews
    final void initValidePhoneActivity() {
        ViewStyleUtil.editTextBindButton(loginButton, editPhone, editCode);
        user = AccountInfo.loadAccount(this);
        sendPhoneMessage.setEditPhone(editPhone);
        sendPhoneMessage.setUrl(ValidePhoneView.CHANGE_PHONE);

        handler2FA = new WeakRefHander(msg -> {
            if (twoFAEdit.getVisibility() == View.VISIBLE) {
                String secret = AccountInfo.loadAuth(this, MyApp.sUserObject.global_key);
                if (secret.isEmpty()) {
                    return true;
                }

                String code2FA = new AuthInfo(secret, new TotpClock(this)).getCode();
                twoFAEdit.setText(code2FA);
            }

            return true;
        }, 100);

        final String url = Global.HOST_API + "/user/2fa/method";
        MyAsyncHttpClient.get(this, url, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                String type = response.optString("data");
                if (type.equals("password")) {
                    passwordEdit.setVisibility(View.VISIBLE);
                    twoFAEdit.setVisibility(View.GONE);
                    ViewStyleUtil.editTextBindButton(loginButton, editPhone, editCode, passwordEdit);
                } else {
                    passwordEdit.setVisibility(View.GONE);
                    twoFAEdit.setVisibility(View.VISIBLE);
                    ViewStyleUtil.editTextBindButton(loginButton, editPhone, editCode, twoFAEdit);
                    handler2FA.sendEmptyMessage(0);
                }
            }
        });


        bindCountry();
    }

    @Click
    void countryCode() {
        CountryPickActivity_.intent(this)
                .startForResult(RESULT_PICK_COUNTRY);
    }

    void bindCountry() {
        countryCode.setText(pickCountry.getCountryCode());
        sendPhoneMessage.setPhoneCountry(pickCountry);
    }

    @OnActivityResult(RESULT_PICK_COUNTRY)
    void onResultPickCountry(int resultCode, @OnActivityResult.Extra PhoneCountry resultData) {
        if (resultCode == Activity.RESULT_OK && resultData != null) {
            pickCountry = resultData;
            bindCountry();
        }
    }

    @Override
    protected void onStop() {
        sendPhoneMessage.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (handler2FA != null) {
            handler2FA.removeMessages(0);
        }

        super.onDestroy();
    }

    @Click
    void loginButton() {
        final String url = Global.HOST_API + "/user/updateInfo";
        RequestParams params = new RequestParams();
        try {
            params.put("phone", user.phone);
            params.put("tags", user.tags);
            params.put("job", user.job);
            params.put("sex", user.sex);
            String phoneString = editPhone.getTextString();
            user.phone = phoneString;
            params.put("phone", user.phone);
            params.put("birthday", user.birthday);
            params.put("location", user.location);
            params.put("company", user.company);
            params.put("slogan", user.slogan);
            params.put("introduction", user.introduction);
            params.put("lavatar", user.lavatar);
            params.put("global_key", user.global_key);
            params.put("name", user.name);
            params.put("email", user.email);
            params.put("id", user.id);
            String phoneCodeString = editCode.getTextString();
            params.put("code", phoneCodeString);

            postNetwork(url, params, TAG_SET_USER_INFO);
            showProgressBar(true, "");
        } catch (Exception e) {
            showMiddleToast(e.toString());
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_SET_USER_INFO)) {
            showProgressBar(false, "");
            if (code == 0) {
                showMiddleToast("修改成功");
                setResult(Activity.RESULT_OK);
                AccountInfo.saveAccount(this, user);
                MyApp.sUserObject = user;
                finish();
            } else {
                showErrorMsgMiddle(code, respanse);
            }
        }
    }
}
