package net.coding.program.push.xiaomi

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.xiaomi.mipush.sdk.*
import org.greenrobot.eventbus.EventBus
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.*

/**
 * 1、PushMessageReceiver 是个抽象类，该类继承了 BroadcastReceiver。<br></br>
 * 2、需要将自定义的 DemoMessageReceiver 注册在 AndroidManifest.xml 文件中：
 * <pre>
 * `<receiver
 * android:name="com.xiaomi.mipushdemo.DemoMessageReceiver"
 * android:exported="true">
 * <intent-filter>
 * <action android:name="com.xiaomi.mipush.RECEIVE_MESSAGE" />
 * </intent-filter>
 * <intent-filter>
 * <action android:name="com.xiaomi.mipush.MESSAGE_ARRIVED" />
 * </intent-filter>
 * <intent-filter>
 * <action android:name="com.xiaomi.mipush.ERROR" />
 * </intent-filter>
 * </receiver>
`</pre> *
 * 3、DemoMessageReceiver 的 onReceivePassThroughMessage 方法用来接收服务器向客户端发送的透传消息。<br></br>
 * 4、DemoMessageReceiver 的 onNotificationMessageClicked 方法用来接收服务器向客户端发送的通知消息，
 * 这个回调方法会在用户手动点击通知后触发。<br></br>
 * 5、DemoMessageReceiver 的 onNotificationMessageArrived 方法用来接收服务器向客户端发送的通知消息，
 * 这个回调方法是在通知消息到达客户端时触发。另外应用在前台时不弹出通知的通知消息到达客户端也会触发这个回调函数。<br></br>
 * 6、DemoMessageReceiver 的 onCommandResult 方法用来接收客户端向服务器发送命令后的响应结果。<br></br>
 * 7、DemoMessageReceiver 的 onReceiveRegisterResult 方法用来接收客户端向服务器发送注册命令后的响应结果。<br></br>
 * 8、以上这些方法运行在非 UI 线程中。
 *
 * @author mayixiang
 */
class XiaomiPushReceiver : PushMessageReceiver() {

    private var mRegId: String? = null
    private val mTopic: String? = null
    private val mAlias: String? = null
    private val mAccount: String? = null
    private val mStartTime: String? = null
    private val mEndTime: String? = null

    private val simpleDate: String
        @SuppressLint("SimpleDateFormat")
        get() = SimpleDateFormat("MM-dd hh:mm:ss").format(Date())

    override fun onReceivePassThroughMessage(context: Context?, message: MiPushMessage?) {
        Log.v(PushAction.TAG,
                "onReceivePassThroughMessage is called. " + message!!.toString())
        //        String log = context.getString(R.string.recv_passthrough_message, message.getContent());
        //        MainActivity.logList.add(0, getSimpleDate() + " " + log);
        //
        //        if (!TextUtils.isEmpty(message.getTopic())) {
        //            mTopic = message.getTopic();
        //        } else if (!TextUtils.isEmpty(message.getAlias())) {
        //            mAlias = message.getAlias();
        //        }
        //
        //        Message msg = Message.obtain();
        //        msg.obj = log;
        //        DemoApplication.getHandler().sendMessage(msg);
    }

    override fun onNotificationMessageClicked(context: Context?, message: MiPushMessage?) {
        Log.v(PushAction.TAG,
                "onNotificationMessageClicked is called. " + message!!.toString())

        val s = message.content
        if (TextUtils.isEmpty(s)) {
            return
        }

        val extra = HashMap<String, String>()
        try {
            val uri = Uri.parse("https://coding.net?" + s)
            val keyUrl = "param_url"
            val paramUrl = uri.getQueryParameter(keyUrl)
            if (!TextUtils.isEmpty(paramUrl)) {
                extra[keyUrl] = URLDecoder.decode(paramUrl)
            }
            val idKey = "notification_id"
            val paramId = uri.getQueryParameter(idKey)
            if (!TextUtils.isEmpty(paramId)) {
                extra[idKey] = paramId
            }
        } catch (e: Exception) {
            Log.e(PushAction.TAG, e.toString())
        }

        if (!extra.isEmpty()) {
            XiaomiPush.clickPushAction?.click(context!!, extra)
        }
    }

    override fun onNotificationMessageArrived(context: Context?, message: MiPushMessage?) {
        Log.v(PushAction.TAG,
                "onNotificationMessageArrived is called. " + message!!.toString())

        //        String log = context.getString(R.string.arrive_notification_message, message.getContent());
        //        MainActivity.logList.add(0, getSimpleDate() + " " + log);
        //
        //        if (!TextUtils.isEmpty(message.getTopic())) {
        //            mTopic = message.getTopic();
        //        } else if (!TextUtils.isEmpty(message.getAlias())) {
        //            mAlias = message.getAlias();
        //        }
        //
        //        Message msg = Message.obtain();
        //        msg.obj = log;
        //        DemoApplication.getHandler().sendMessage(msg);
    }

    override fun onCommandResult(context: Context?, message: MiPushCommandMessage?) {
        Log.v(PushAction.TAG,
                "onCommandResult is called. " + message!!.toString())
        //        String command = message.getCommand();
        //        List<String> arguments = message.getCommandArguments();
        //        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        //        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        //        String log;
        //        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
        //            if (message.getResultCode() == ErrorCode.SUCCESS) {
        //                mRegId = cmdArg1;
        //                log = context.getString(R.string.register_success);
        //            } else {
        //                log = context.getString(R.string.register_fail);
        //            }
        //        } else if (MiPushClient.COMMAND_SET_ALIAS.equals(command)) {
        //            if (message.getResultCode() == ErrorCode.SUCCESS) {
        //                mAlias = cmdArg1;
        //                log = context.getString(R.string.set_alias_success, mAlias);
        //            } else {
        //                log = context.getString(R.string.set_alias_fail, message.getReason());
        //            }
        //        } else if (MiPushClient.COMMAND_UNSET_ALIAS.equals(command)) {
        //            if (message.getResultCode() == ErrorCode.SUCCESS) {
        //                mAlias = cmdArg1;
        //                log = context.getString(R.string.unset_alias_success, mAlias);
        //            } else {
        //                log = context.getString(R.string.unset_alias_fail, message.getReason());
        //            }
        //        } else if (MiPushClient.COMMAND_SET_ACCOUNT.equals(command)) {
        //            if (message.getResultCode() == ErrorCode.SUCCESS) {
        //                mAccount = cmdArg1;
        //                log = context.getString(R.string.set_account_success, mAccount);
        //            } else {
        //                log = context.getString(R.string.set_account_fail, message.getReason());
        //            }
        //        } else if (MiPushClient.COMMAND_UNSET_ACCOUNT.equals(command)) {
        //            if (message.getResultCode() == ErrorCode.SUCCESS) {
        //                mAccount = cmdArg1;
        //                log = context.getString(R.string.unset_account_success, mAccount);
        //            } else {
        //                log = context.getString(R.string.unset_account_fail, message.getReason());
        //            }
        //        } else if (MiPushClient.COMMAND_SUBSCRIBE_TOPIC.equals(command)) {
        //            if (message.getResultCode() == ErrorCode.SUCCESS) {
        //                mTopic = cmdArg1;
        //                log = context.getString(R.string.subscribe_topic_success, mTopic);
        //            } else {
        //                log = context.getString(R.string.subscribe_topic_fail, message.getReason());
        //            }
        //        } else if (MiPushClient.COMMAND_UNSUBSCRIBE_TOPIC.equals(command)) {
        //            if (message.getResultCode() == ErrorCode.SUCCESS) {
        //                mTopic = cmdArg1;
        //                log = context.getString(R.string.unsubscribe_topic_success, mTopic);
        //            } else {
        //                log = context.getString(R.string.unsubscribe_topic_fail, message.getReason());
        //            }
        //        } else if (MiPushClient.COMMAND_SET_ACCEPT_TIME.equals(command)) {
        //            if (message.getResultCode() == ErrorCode.SUCCESS) {
        //                mStartTime = cmdArg1;
        //                mEndTime = cmdArg2;
        //                log = context.getString(R.string.set_accept_time_success, mStartTime, mEndTime);
        //            } else {
        //                log = context.getString(R.string.set_accept_time_fail, message.getReason());
        //            }
        //        } else {
        //            log = message.getReason();
        //        }
        //        MainActivity.logList.add(0, getSimpleDate() + "    " + log);
        //
        //        Message msg = Message.obtain();
        //        msg.obj = log;
        //        DemoApplication.getHandler().sendMessage(msg);
    }

    override fun onReceiveRegisterResult(context: Context?, message: MiPushCommandMessage?) {
        Log.v(PushAction.TAG,
                "onReceiveRegisterResult is called. " + message!!.toString())

        if (MiPushClient.COMMAND_REGISTER == message.command &&
                message.resultCode == ErrorCode.SUCCESS.toLong()) {
            message.commandArguments?.let {
                if (it.size > 0) mRegId = it[0] else mRegId = null
            }
            EventBus.getDefault().postSticky(EventPushToken("xiaomi", MiPushClient.getRegId(context)))
        }
    }

}
