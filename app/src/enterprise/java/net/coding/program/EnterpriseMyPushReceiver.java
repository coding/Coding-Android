package net.coding.program;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import net.coding.program.common.Global;
import net.coding.program.common.network.MyAsyncHttpClient;

public class EnterpriseMyPushReceiver extends BroadcastReceiver {

    public static final String PushClickBroadcast = "net.coding.program.EnterpriseMyPushReceiver.PushClickBroadcast";

    public EnterpriseMyPushReceiver() {
    }

    public static void closeNotify(Context context, String url) {
        String notifys[] = EnterprisePushReceiver.sNotify;
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        for (int i = 0; i < notifys.length; ++i) {
            if (url.equals(notifys[i])) {
                mNotificationManager.cancel(i);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String url = intent.getStringExtra("data");

        if (url != null) {
            MyApp.openNewActivityFromMain(context, url);
        }

        String id = intent.getStringExtra("id");
        if (id != null && !id.isEmpty()) {
            AsyncHttpClient client = MyAsyncHttpClient.createClient(context);
            final String host = Global.HOST_API + "/notification/mark-read?id=%s";
            client.post(String.format(host, id), new JsonHttpResponseHandler() {
            });
        }
    }
}
