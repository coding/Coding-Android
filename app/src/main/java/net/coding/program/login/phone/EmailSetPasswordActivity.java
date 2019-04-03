package net.coding.program.login.phone;

import android.graphics.BitmapFactory;
import android.text.Editable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.LoginEditText;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.InputCheck;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

@EActivity(R.layout.activity_email_set_password)
public class EmailSetPasswordActivity extends BackActivity {

    @ViewById
    protected LoginEditText emailEdit, captchaEdit;
    @ViewById
    protected View loginButton;
    @ViewById
    ImageView captchaImage;
    @Extra
    String account = "";

    @AfterViews
    void initEmailSetPasswordActivity() {
        emailEdit.setText(account);
        captchaEdit.requestFocus();
        requestCaptcha();
    }

    @AfterTextChange({R.id.emailEdit, R.id.captchaEdit})
    void validFirstStepButton(TextView tv, Editable text) {
        loginButton.setEnabled(InputCheck.checkEditIsFill(emailEdit, captchaEdit));
    }

    protected String getUrl() {
        return Global.HOST_API + "/account/password/forget";
    }

    @Click
    void loginButton() {
        String emailString = emailEdit.getText().toString();
        String captchaString = captchaEdit.getText().toString();
        if (!InputCheck.checkEmail(this, emailString)) {
            return;
        }

        String url = getUrl();
        RequestParams params = new RequestParams();
        params.put("account", emailString);
        params.put("j_captcha", captchaString);
        MyAsyncHttpClient.post(this, url, params, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                setResult(RESULT_OK);
                showMiddleToastLong("重置密码邮件已经发送，请尽快去邮箱查看");
                finish();
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
                requestCaptcha();
                captchaEdit.requestFocus();
            }

            @Override
            public void onFinish() {
                super.onFinish();
                showProgressBar(false, "");
            }
        });

        showProgressBar(true, "");
    }

    private void requestCaptcha() {
        captchaEdit.setText("");
        String url = Global.HOST_API + "/getCaptcha";
        MyAsyncHttpClient.get(this, url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                captchaImage.setImageBitmap(BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    @Click
    void captchaImage() {
        requestCaptcha();
    }

    @Click
    void backImage() {
        finish();
    }

}
