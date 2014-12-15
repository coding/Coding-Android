package net.coding.program.common;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

import net.coding.program.Global;
import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.model.AccountInfo;

import org.json.JSONObject;

/**
 * Created by chaochen on 14-12-5.
 */
public class PushReceiver extends XGPushBaseReceiver {

    public void onRegisterResult(Context context, int i, XGPushRegisterResult xgPushRegisterResult) {
        Log.d("", "" + context);
    }

    public void onUnregisterResult(Context context, int i) {
        Log.d("", "" + context);

    }

    public void onSetTagResult(Context context, int i, String s) {
        Log.d("", "" + context);

    }

    public void onDeleteTagResult(Context context, int i, String s) {
        Log.d("", "" + context);
    }

    public void onTextMessage(Context context, XGPushTextMessage xgPushTextMessage) {
        Log.d("", "" + context);

        try {
            if (!AccountInfo.getNeedPush(context)) {
                return;
            }

            issueNotification(context, xgPushTextMessage);

        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    public void onNotifactionClickedResult(Context context, XGPushClickedResult xgPushClickedResult) {
        Log.d("", "" + context);

    }

    public void onNotifactionShowedResult(Context context, XGPushShowedResult xgPushShowedResult) {
        Log.d("", "" + context);

    }

    NotificationCompat.Builder builder;

    private void issueNotification(Context context, XGPushTextMessage message) {
        String title = message.getTitle();
        String msg = message.getContent();
        msg = msg.replaceAll("<img src='(.*?)'/>", "[$1]");

        String id = "";
        String url = "";
        try {
            JSONObject jsonCustom = new JSONObject(message.getCustomContent());
            id = jsonCustom.optString("notification_id");
            url = jsonCustom.optString("param_url");

        } catch (Exception e) {
            Global.errorLog(e);
        }

        if (url.isEmpty()) {
            Log.e("", "收到空消息");
//            return;
        }

        builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(msg)
                .setDefaults(Notification.DEFAULT_ALL);


        Intent resultIntent = new Intent(MyApp.PushClickBroadcast);
        resultIntent.putExtra("data", url);
        resultIntent.putExtra("id", id);

        notifyId++;
        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                context,
                notifyId,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        builder.setContentIntent(resultPendingIntent);
        builder.setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notifyIdInt = notifyId % 5;
        sNotify[notifyIdInt] = url;
        mNotificationManager.notify(notifyIdInt, builder.build());
    }

    static int notifyId = 0;

    public static String sNotify[] = new String[5];
}