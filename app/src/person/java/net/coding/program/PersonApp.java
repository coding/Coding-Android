package net.coding.program;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import net.coding.program.common.Global;
import net.coding.program.common.htmltext.URLSpanNoUnderline;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.compatible.CodingCompatImp;
import net.coding.program.push.CodingPush;
import net.coding.program.push.xiaomi.CommonPushClick;

import java.util.Map;

public class PersonApp extends MyApp {

    @Override
    public void onCreate() {
        super.onCreate();

        CodingCompat.init(new CodingCompatImp());

        CodingPush.instance().initApplication(this, (context, params) -> {
            if (params == null) {
                return;
            }
            String url = params.get("param_url");
            String id = params.get("notification_id");
            if (TextUtils.isEmpty(url)) {
                return;
            }

            if (url != null) {
                if (MyApp.getMainActivityState()) {
                    URLSpanNoUnderline.openActivityByUri(context, url, true);
                } else {
                    Intent mainIntent = new Intent(context, CodingCompat.instance().getMainActivity());
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(mainIntent);
                    URLSpanNoUnderline.openActivityByUri(context, url, true);
                }
            }

            if (id != null && !id.isEmpty()) {
                AsyncHttpClient client = MyAsyncHttpClient.createClient(context);
                final String host = Global.HOST_API + "/notification/mark-read?id=%s";
                client.post(String.format(host, id), new JsonHttpResponseHandler() {
                });
            }

        });

    }
}
