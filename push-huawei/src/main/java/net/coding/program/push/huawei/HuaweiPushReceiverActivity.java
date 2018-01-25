package net.coding.program.push.huawei;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import net.coding.program.push.xiaomi.PushAction;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenchao on 2017/11/9.
 */

public class HuaweiPushReceiverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        jumpTargetActivity();
        finish();
    }

    private void jumpTargetActivity() {
        Uri uri = getIntent().getData();

        Map<String, String> map = new HashMap<>();

        Log.d(PushAction.Companion.getTAG(), "HuaweiPushReceiverActivity " + uri.toString());
        try {
            if (uri != null) {
                final String paramUrl = "param_url";
                String urlString = uri.getQueryParameter(paramUrl);
                if (!TextUtils.isEmpty(urlString)) {
                    map.put(paramUrl, URLDecoder.decode(urlString));
                }

                final String notificationId = "notification_id";
                String idString = uri.getQueryParameter(notificationId);
                if (!TextUtils.isEmpty(idString)) {
                    map.put(notificationId, URLDecoder.decode(idString));
                }

                HuaweiPush.instance().click(this, map);
            }
        } catch (Exception e) {
            Log.d(PushAction.Companion.getTAG(), e.toString());
        }
    }


}
