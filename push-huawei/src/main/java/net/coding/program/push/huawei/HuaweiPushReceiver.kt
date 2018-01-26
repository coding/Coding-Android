package net.coding.program.push.huawei

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.huawei.hms.support.api.push.PushReceiver
import net.coding.program.push.xiaomi.EventPushToken
import org.greenrobot.eventbus.EventBus

class HuaweiPushReceiver : PushReceiver() {

    override fun onToken(context: Context, token: String, extras: Bundle?) {
        val belongId = extras!!.getString("belongId")
        Log.i(HuaweiPush.TAG, "belongId为:" + belongId!!)
        Log.i(HuaweiPush.TAG, "Token为:" + token)
        HuaweiPush.setToken(token)
        EventBus.getDefault().postSticky(EventPushToken("huawei", token))

        val intent = Intent()
        intent.action = ACTION_UPDATEUI
        intent.putExtra("type", 1)
        intent.putExtra("token", token)
        context.sendBroadcast(intent)
    }

    // 不使用透传消息
    override fun onPushMsg(context: Context, msg: ByteArray, bundle: Bundle): Boolean {
        try {
            //CP可以自己解析消息内容，然后做相应的处理
            val content = String(msg)
            Log.i(HuaweiPush.TAG, "收到PUSH透传消息,消息内容为:" + content)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    override fun onEvent(context: Context?, event: PushReceiver.Event?, extras: Bundle?) {
        if (PushReceiver.Event.NOTIFICATION_OPENED == event || PushReceiver.Event.NOTIFICATION_CLICK_BTN == event) {
            val notifyId = extras!!.getInt(PushReceiver.BOUND_KEY.pushNotifyId, 0)
            Log.i(HuaweiPush.TAG, "收到通知栏消息点击事件,notifyId:" + notifyId)
            //            if (0 != notifyId) {
            //                NotificationManager manager = (NotificationManager) context
            //                        .getSystemService(Context.NOTIFICATION_SERVICE);
            //                manager.cancel(notifyId);
            //            }
        }

        val message = extras!!.getString(PushReceiver.BOUND_KEY.pushMsgKey)
        super.onEvent(context, event, extras)

        val sb = StringBuilder()
        for (s in extras.keySet()) {
            try {
                sb.append(s)
                sb.append("\t")
                sb.append(extras.getString(s))
                sb.append("\n")
            } catch (e: Exception) {
                Log.d(HuaweiPush.TAG, "error " + e.toString())
            }

        }
        Log.d(HuaweiPush.TAG, sb.toString())
    }

    override fun onPushState(context: Context?, pushState: Boolean) {
        Log.i(HuaweiPush.TAG, "Push连接状态为:" + pushState)

        val intent = Intent()
        intent.action = ACTION_UPDATEUI
        intent.putExtra("type", 2)
        intent.putExtra("pushState", pushState)
        context!!.sendBroadcast(intent)
    }

    companion object {

        val ACTION_UPDATEUI = "action.updateUI"
    }

}
