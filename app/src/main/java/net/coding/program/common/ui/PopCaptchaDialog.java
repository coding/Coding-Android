package net.coding.program.common.ui;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.util.SingleToast;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by chenchao on 16/6/6.
 */
public class PopCaptchaDialog {

    public static void pop(Context context) {
        View layout = LayoutInflater.from(context).inflate(R.layout.dialog_captcha, null);
        EditText captchaEdit = (EditText) layout.findViewById(R.id.captchaEdit);
        ImageView captchaImage = (ImageView) layout.findViewById(R.id.imageValify);

        captchaImage.setOnClickListener(v -> {
            String url = Global.HOST_API + "/getCaptcha?type=0";
            MyAsyncHttpClient.get(context, url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    captchaImage.setImageBitmap(BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length));
                    captchaEdit.setText("");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    SingleToast.showMiddleToast(context, "获取验证码失败");
                }
            });
        });

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(layout)
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null)
                .show();

        Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(view -> {
            String input = captchaEdit.getText().toString();
            if (input.isEmpty()) {
                return;
            }

            String url = Global.HOST_API + "/request_valid?type=0";
            RequestParams params = new RequestParams();
            params.put("j_captcha", input);
            MyAsyncHttpClient.post(context, url, params, new MyJsonResponse(context) {
                @Override
                public void onMySuccess(JSONObject response) {
                    dialog.dismiss();
                }
            });

        });

        captchaImage.performClick();
    }
}
