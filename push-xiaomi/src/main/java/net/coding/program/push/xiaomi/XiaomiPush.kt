package net.coding.program.push.xiaomi

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.util.Log
import com.xiaomi.channel.commonutils.logger.LoggerInterface
import com.xiaomi.mipush.sdk.Logger
import com.xiaomi.mipush.sdk.MiPushClient

class XiaomiPush : PushAction {

    override fun init(context: Context, clickPushActionName: CommonPushClick): Boolean {
        // 注册push服务，注册成功后会向DemoMessageReceiver发送广播
        // 可以从DemoMessageReceiver的onCommandResult方法中MiPushCommandMessage对象参数中获取注册信息
        if (shouldInit(context)) {
            MiPushClient.registerPush(context, APP_ID, APP_KEY)

            val newLogger = object : LoggerInterface {

                override fun setTag(tag: String) {
                    // ignore
                }

                override fun log(content: String, t: Throwable) {
                    Log.d(PushAction.TAG, content, t)
                }

                override fun log(content: String) {
                    Log.d(PushAction.TAG, content)
                }
            }
            Logger.setLogger(context, newLogger)
            Log.d(PushAction.TAG, "register xiaomi push no exception, wait result ...")

            clickPushAction = clickPushActionName

            return true
        }
        Log.d(PushAction.TAG, "register xiaomi push false")

        return false
    }

    override fun bindGK(context: Context, gk: String) {
        Log.d(PushAction.TAG, "use xiaomi push bind " + gk)
        MiPushClient.setUserAccount(context, gk, null)
    }

    override fun unbindGK(context: Context, gk: String) {
        Log.d(PushAction.TAG, "use xiaomi push unbind " + gk)
        MiPushClient.unsetUserAccount(context, gk, null)
    }

    private fun shouldInit(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processInfos = am.runningAppProcesses
        val mainProcessName = context.packageName
        val myPid = Process.myPid()
        for (info in processInfos) {
            if (info.pid == myPid && mainProcessName == info.processName) {
                return true
            }
        }
        return false
    }

    companion object {

        // user your appid the key.
        private val APP_ID = "2882303761517260238"
        // user your appid the key.
        private val APP_KEY = "5861726013238"

        var clickPushAction: CommonPushClick? = null
    }

}
