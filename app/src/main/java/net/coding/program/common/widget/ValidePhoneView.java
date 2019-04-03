package net.coding.program.common.widget;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.model.PhoneCountry;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.common.ui.PopCaptchaDialog;
import net.coding.program.common.util.InputCheck;
import net.coding.program.common.util.OnTextChange;

import org.json.JSONObject;

/**
 * Created by chenchao on 16/1/4.
 * 发送短信验证码的按钮
 */
public class ValidePhoneView extends AppCompatTextView {

    OnTextChange editPhone;
    String inputPhone = "";
    PhoneCountry pickCountry = PhoneCountry.getChina();
    private MyJsonResponse parseJson;
    private Type type = Type.normal;
    private CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {

        public void onTick(long millisUntilFinished) {
            ValidePhoneView.this.setText(String.format("%s秒", millisUntilFinished / 1000));
            ValidePhoneView.this.setEnabled(false);
        }

        public void onFinish() {
            ValidePhoneView.this.setEnabled(true);
            ValidePhoneView.this.setText("发送验证码");
        }
    };

    public ValidePhoneView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOnClickListener(v -> sendPhoneMessage(""));
    }

    public void setEditPhone(OnTextChange edit) {
        editPhone = edit;
    }

    public void setPhoneCountry(PhoneCountry phoneCountry) {
        pickCountry = phoneCountry;
    }

    public void setPhoneString(String phone) {
        inputPhone = phone;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void startTimer() {
        countDownTimer.cancel();
        countDownTimer.start();
    }

    public void onStop() {
        countDownTimer.cancel();
        countDownTimer.onFinish();
    }

    void sendPhoneMessage(String captche) {
        if (inputPhone.isEmpty() && editPhone == null) {
            Log.e("", "editPhone is null");
            return;
        }

        String phoneString;
        if (editPhone != null) {
            phoneString = editPhone.getText().toString();
        } else {
            phoneString = inputPhone;
        }

        if (!InputCheck.checkPhone(getContext(), phoneString)) return;

        RequestParams params = new RequestParams();

        AsyncHttpClient client = MyAsyncHttpClient.createClient(getContext());

        if (parseJson == null) {
            parseJson = new MyJsonResponse(getContext()) {
                @Override
                public void onMySuccess(JSONObject response) {
                    super.onMySuccess(response);
                    net.coding.program.common.util.SingleToast.showMiddleToast(getContext(), "验证码已发送");
                }

                @Override
                public void onMyFailure(JSONObject response) {
                    countDownTimer.cancel();
                    countDownTimer.onFinish();

                    if (response.optInt("code", 0) == NetworkImpl.NETWORK_ERROR_SEND_MESSAGE_NEED_CAPTCHA) {
                        PopCaptchaDialog.pop(getContext(), (input, dialog) -> {
                            ValidePhoneView.this.sendPhoneMessage(input);
                            dialog.dismiss();
                        });
                    } else {
                        super.onMyFailure(response);
                    }
                }
            };
        }

        if (!TextUtils.isEmpty(captche)) {
            params.put("j_captcha", captche);
        }

        if (type == Type.setPassword) {
            params.put("phoneCountryCode", pickCountry.getCountryCode());
            params.put("account", phoneString);
        } else if (type == Type.register) {
            params.put("phoneCountryCode", pickCountry.getCountryCode());
            params.put("phone", phoneString);
            params.put("from", "coding");
        } else if (type == Type.valide) {
            params.put("phoneCountryCode", pickCountry.getCountryCode());
            params.put("phone", phoneString);
        } else if (type == Type.close2FA) {
            params.put("phoneCountryCode", pickCountry.getCountryCode());
            params.put("phone", phoneString);
        }
        client.post(getContext(), type.url, params, parseJson);

        countDownTimer.start();
    }

    public enum Type {
        normal(Global.HOST_API + "/user/generate_phone_code"),
        register(Global.HOST_API + "/account/register/generate_phone_code"),
        setPassword(Global.HOST_API + "/account/password/forget"),
        close2FA(Global.HOST_API + "/twofa/close/code"),
        valide(Global.HOST_API + "/account/phone/change/code");

        String url = "";

        Type(String url) {
            this.url = url;
        }
    }
}
