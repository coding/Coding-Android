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

import net.coding.program.MyPushReceiver;
import net.coding.program.R;
import net.coding.program.common.htmltext.URLSpanNoUnderline;
import net.coding.program.message.UsersListFragment;
import net.coding.program.model.AccountInfo;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void onTextMessage(Context context, XGPushTextMessage message) {
        Log.d("", "" + context);

        try {
            if (!AccountInfo.getNeedPush(context) || !AccountInfo.isLogin(context)) {
                return;
            }

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
                return;
            }

            Pattern pattern = Pattern.compile(URLSpanNoUnderline.PATTERN_URL_MESSAGE);
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                String noNotifyGlobalKey = GlobalSetting.getInstance().getMessageNotify();
                if (noNotifyGlobalKey.equals(matcher.group(1))) {
                    return;
                }
            }

            showNotify(context, title, msg, id, url);

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

    private void showNotify(Context context, String title, String msg, String id, String url) {
        builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(msg)
                .setDefaults(Notification.DEFAULT_ALL);


        Intent resultIntent = new Intent(MyPushReceiver.PushClickBroadcast);
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

        int notifyIdInt = -1;
        for (int i = 0; i < sNotify.length; ++i) {
            if (url.equals(sNotify[i])) {
                notifyIdInt = i;
                sNotify[notifyIdInt] = url;
                break;
            }
        }
        if (notifyIdInt == -1) {
            notifyIdInt = notifyId % 5;
            sNotify[notifyIdInt] = url;
        }

        Pattern pattern = Pattern.compile(URLSpanNoUnderline.PATTERN_URL_MESSAGE);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String globalKey = matcher.group(1);

            UsersListFragment.receiverMessagePush(globalKey, msg);
        }

        mNotificationManager.notify(notifyIdInt, builder.build());
    }

    static int notifyId = 0;

    public static String sNotify[] = new String[5];
}