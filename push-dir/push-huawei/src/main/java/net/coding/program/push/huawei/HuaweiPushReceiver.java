package net.coding.program.push.huawei;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import net.coding.program.push.huawei.*;

import com.huawei.hms.support.api.push.PushReceiver;

public class HuaweiPushReceiver extends PushReceiver {

	public static final String ACTION_UPDATEUI = "action.updateUI";
    @Override
    public void onToken(Context context, String token, Bundle extras) {
    	String belongId = extras.getString("belongId");
        Log.i(HuaweiPush.TAG, "belongId为:" + belongId);
        Log.i(HuaweiPush.TAG, "Token为:" + token);

        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATEUI);
        intent.putExtra("type", 1);
        intent.putExtra("token", token);
        context.sendBroadcast(intent);
    }

    @Override
    public boolean onPushMsg(Context context, byte[] msg, Bundle bundle) {
        try {
        	//CP可以自己解析消息内容，然后做相应的处理
            String content = new String(msg, "UTF-8");
            Log.i(HuaweiPush.TAG, "收到PUSH透传消息,消息内容为:" + content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void onEvent(Context context, Event event, Bundle extras) {
        if (Event.NOTIFICATION_OPENED.equals(event) || Event.NOTIFICATION_CLICK_BTN.equals(event)) {
            int notifyId = extras.getInt(BOUND_KEY.pushNotifyId, 0);
        	Log.i(HuaweiPush.TAG, "收到通知栏消息点击事件,notifyId:" + notifyId);
            if (0 != notifyId) {
                NotificationManager manager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(notifyId);
            }
        }

        String message = extras.getString(BOUND_KEY.pushMsgKey);
        super.onEvent(context, event, extras);
    }

    @Override
    public void onPushState(Context context, boolean pushState) {
    	Log.i(HuaweiPush.TAG, "Push连接状态为:" + pushState);

        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATEUI);
        intent.putExtra("type", 2);
        intent.putExtra("pushState", pushState);
        context.sendBroadcast(intent);
    }

}
