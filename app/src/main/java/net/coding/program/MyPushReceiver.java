package net.coding.program;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import net.coding.program.common.Global;
import net.coding.program.common.PushReceiver;
import net.coding.program.common.htmltext.URLSpanNoUnderline;
import net.coding.program.common.network.MyAsyncHttpClient;

public class MyPushReceiver extends BroadcastReceiver {

    public static final String PushClickBroadcast = "net.coding.program.MyPushReceiver.PushClickBroadcast";

    public MyPushReceiver() {
    }

    public static void closeNotify(Context context, String url) {
        String notifys[] = PushReceiver.sNotify;
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
            closeNotify(context, url);
            if (MyApp.getMainActivityState()) {
                URLSpanNoUnderline.openActivityByUri(context, url, true);
            } else {
                Intent mainIntent = new Intent(context, MainActivity_.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                mainIntent.putExtra("mPushUrl", url);
                context.startActivity(mainIntent);
                URLSpanNoUnderline.openActivityByUri(context, url, true);
            }
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
