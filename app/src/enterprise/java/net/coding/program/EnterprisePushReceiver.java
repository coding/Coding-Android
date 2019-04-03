package net.coding.program;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.GlobalSetting;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.push.PushUrl;
import net.coding.program.message.UsersListFragment;
import net.coding.program.route.URLSpanNoUnderline;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chaochen on 14-12-5.
 */

public class EnterprisePushReceiver extends XGPushBaseReceiver {

    public static String sNotify[] = new String[5];
    static int notifyId = 0;
    private static long sLastNotify = 0;
    NotificationCompat.Builder builder;

    @Override
    public void onRegisterResult(Context context, int i, XGPushRegisterResult xgPushRegisterResult) {
        Log.d("", "" + context);
    }

    @Override
    public void onUnregisterResult(Context context, int i) {
        Log.d("", "" + context);
    }

    @Override
    public void onSetTagResult(Context context, int i, String s) {
        Log.d("", "" + context);
    }

    @Override
    public void onDeleteTagResult(Context context, int i, String s) {
        Log.d("", "" + context);
    }

    @Override
    public void onTextMessage(Context context, XGPushTextMessage message) {
        try {
            if (!AccountInfo.isLogin(context)) {
                return;
            }

            Logger.d("ccccc" + message);
            // 这个写法是为了方便调试，因为信鸽的后台不能输入 customContent
            JSONObject jsonCustom = null;
            try {
                jsonCustom = new JSONObject(message.getCustomContent());
            } catch (Exception e) {
            }
            if (jsonCustom == null) {
                jsonCustom = new JSONObject(message.getContent());
            }

            if (jsonCustom.has("cancel")) {
                String cancelString = jsonCustom.optString("cancel");
                if (cancelString.equals("message")) {
                    cancel(context, true);
                } else {
                    cancel(context, false);
                }

                return;
            }

            String id = jsonCustom.optString("notification_id", "");
            String url = jsonCustom.optString("param_url", "");

            // 如果本地没有 2FA，就不显示这个推送
            if (PushUrl.INSTANCE.is2faLink(url)) {
                String authUri = AccountInfo.loadAuth(context, GlobalData.sUserObject.global_key);
                if (authUri.isEmpty()) {
                    return;
                }
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

            String title = message.getTitle();
            String msg = message.getContent();
            msg = msg.replaceAll("<img src='(.*?)'/>", "[$1]");

            showNotify(context, title, msg, id, url);

        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    private void cancel(Context ctx, boolean cannelMessage) {
        String message = URLSpanNoUnderline.PATTERN_URL_MESSAGE;
        Pattern pattern = Pattern.compile(message);
        NotificationManager notificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        for (int i = 0; i < sNotify.length; ++i) {
            if (sNotify[i] == null) {
                continue;
            }

            Matcher matcher = pattern.matcher(sNotify[i]);
            if (matcher.find() == cannelMessage) {
                notificationManager.cancel(i);
            }
        }
    }

    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult xgPushClickedResult) {
        Log.d("", xgPushClickedResult + " || cccccccc || " + xgPushClickedResult.getCustomContent());

    }

    @Override
    public void onNotifactionShowedResult(Context context, XGPushShowedResult xgPushShowedResult) {
        Log.d("", xgPushShowedResult + " || sssssssss || " + xgPushShowedResult.getCustomContent());

    }

    private void showNotify(Context context, String title, String msg, String id, String url) {
        builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.push_icon)
                .setContentTitle(title)
                .setContentText(msg);

        long time = Calendar.getInstance().getTimeInMillis();
        if (time - sLastNotify <= 10 * 1000) { // 小于10秒很可能是联网后一次收到了多个推送，只用呼吸灯提示
            builder.setDefaults(Notification.DEFAULT_LIGHTS);
        } else {
            builder.setDefaults(Notification.DEFAULT_ALL);
        }
        sLastNotify = time;

        Intent resultIntent = new Intent(EnterpriseMyPushReceiver.PushClickBroadcast);
        resultIntent.setPackage(context.getPackageName());
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
}
