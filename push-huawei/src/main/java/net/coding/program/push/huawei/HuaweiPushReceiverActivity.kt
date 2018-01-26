package net.coding.program.push.huawei

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import net.coding.program.push.xiaomi.PushAction
import java.net.URLDecoder
import java.util.*

/**
 * Created by chenchao on 2017/11/9.
 */

class HuaweiPushReceiverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        jumpTargetActivity()
        finish()
    }

    private fun jumpTargetActivity() {
        val uri = intent.data

        val map = HashMap<String, String>()

        Log.d(PushAction.TAG, "HuaweiPushReceiverActivity " + uri!!.toString())
        try {
            val paramUrl = "param_url"
            val urlString = uri.getQueryParameter(paramUrl)
            if (!TextUtils.isEmpty(urlString)) {
                map[paramUrl] = URLDecoder.decode(urlString)
            }

            val notificationId = "notification_id"
            val idString = uri.getQueryParameter(notificationId)
            if (!TextUtils.isEmpty(idString)) {
                map[notificationId] = URLDecoder.decode(idString)
            }

            HuaweiPush.click(this, map)
        } catch (e: Exception) {
            Log.d(PushAction.TAG, e.toString())
        }

    }


}
