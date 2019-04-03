package net.coding.program.push

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import net.coding.program.push.huawei.HuaweiPush
import net.coding.program.push.xiaomi.CommonPushClick
import net.coding.program.push.xiaomi.EventUnbindToken
import net.coding.program.push.xiaomi.PushAction
import net.coding.program.push.xiaomi.XiaomiPush
import org.greenrobot.eventbus.EventBus

/**
 * Created by chenchao on 2017/11/2.
 */

object CodingPush {

    //    private var context: Context? = null
    internal var pushAction: PushAction? = null
    internal var usePush: Boolean = false // 企业版不使用，用户在设置里面设置了不使用也不启用
    internal var clickPush: CommonPushClick? = null

    fun initApplication(context: Context, clickPushAction: CommonPushClick) {
        this.clickPush = clickPushAction
        if (Rom.isEmui()) {
            Log.d(PushAction.TAG, "use huawei push")
            HuaweiPush.initApplication(clickPushAction)
        } else {  // default device use xiaomi push
            if (pushAction == null) {
                pushAction = XiaomiPush()
            }
            if (pushAction!!.init(context, clickPushAction)) {
            }
        }
    }

    fun onCreate(context: Activity, gk: String) {
        if (Rom.isEmui()) {
            HuaweiPush.onCreate(context, gk)
        }
    }

    fun onDestroy() {
        if (Rom.isEmui()) {
            HuaweiPush.onDestroy()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return if (Rom.isEmui()) {
            HuaweiPush.onActivityResult(requestCode, resultCode, data)
        } else false

    }

    fun bindGK(context: Context, gk: String) {
        if (Rom.isEmui()) {
            HuaweiPush.requestToken()
        } else {
            pushAction?.bindGK(context, gk)
        }
    }

    fun unbindGK(context: Context, gk: String) {
        if (Rom.isEmui()) {
            HuaweiPush.deleteToken()
        } else {
            pushAction?.unbindGK(context, gk)
        }

        EventBus.getDefault().postSticky(EventUnbindToken())
    }

}
