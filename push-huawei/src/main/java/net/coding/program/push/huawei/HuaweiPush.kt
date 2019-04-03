package net.coding.program.push.huawei

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import com.huawei.hms.api.ConnectionResult
import com.huawei.hms.api.HuaweiApiAvailability
import com.huawei.hms.api.HuaweiApiClient
import com.huawei.hms.support.api.push.PushException
import net.coding.program.push.xiaomi.CommonPushClick
import java.lang.ref.WeakReference

/**
 * Created by chenchao on 2017/11/6.
 */

object HuaweiPush : HuaweiApiClient.ConnectionCallbacks, HuaweiApiClient.OnConnectionFailedListener, CommonPushClick {

    private val REQUEST_HMS_RESOLVE_ERROR = 1000
    //如果CP在onConnectionFailed调用了resolveError接口，那么错误结果会通过onActivityResult返回
    //具体的返回码通过该字段获取
    val EXTRA_RESULT = "intent.extra.RESULT"

    val TAG = "CodingPush huawei"

    //华为移动服务Client
    private var client: HuaweiApiClient? = null
    private var activity: WeakReference<Activity>? = null
    private var codingGK: String? = null

    private var pushClick: CommonPushClick? = null

    private var token = ""

    val isActivityExit: Boolean
        get() = activity?.get()?.isFinishing ?: true

    fun setToken(token: String) {
        this.token = token
    }

    override fun click(context: Context, params: Map<String, String>) {
        pushClick!!.click(context, params)
    }

    fun initApplication(pushClick: CommonPushClick) {
        this.pushClick = pushClick
    }

    fun onCreate(activity: Activity, gk: String) {
        this.activity = WeakReference(activity)
        codingGK = gk

        //创建华为移动服务client实例用以使用华为push服务
        //需要指定api为HuaweiId.PUSH_API
        //连接回调以及连接失败监听
        client = HuaweiApiClient.Builder(activity)
                .addApi(com.huawei.hms.support.api.push.HuaweiPush.PUSH_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()

        //建议在oncreate的时候连接华为移动服务
        //业务可以根据自己业务的形态来确定client的连接和断开的时机，但是确保connect和disconnect必须成对出现
        client?.connect()

        //        try {
        //            TelephonyManager tm = (TelephonyManager) activity.getSystemService(TELEPHONY_SERVICE);
        //            String imei = tm.getDeviceId();//String
        //            Log.i(TAG, "imei " + imei);
        //        } catch (Exception e) {
        //            Log.i(TAG, "imei 获取失败");
        //        }

    }

    fun onDestroy() {
        client?.disconnect()
    }

    /**
     * 使用同步接口来获取pushtoken
     * 结果通过广播的方式发送给应用，不通过标准接口的pendingResul返回
     * CP可以自行处理获取到token
     * 同步获取token和异步获取token的方法CP只要根据自身需要选取一种方式即可
     */
    fun requestToken() {
        if (client == null) return

        if (!client!!.isConnected) {
            Log.i(TAG, "获取token失败，原因：HuaweiApiClient未连接")
            client!!.connect()
            return
        }

        //需要在子线程中调用函数
        object : Thread() {

            override fun run() {
                Log.i(TAG, "同步接口获取push token")
                val tokenResult = com.huawei.hms.support.api.push.HuaweiPush.HuaweiPushApi.getToken(client)
                val result = tokenResult.await()
                if (result.tokenRes.retCode == 0) {
                    //当返回值为0的时候表明获取token结果调用成功
                    Log.i(TAG, "获取push token 成功，等待广播")
                }

                setReceiveNotifyMsg(true)
            }
        }.start()
    }

    override fun onConnected() {
        //华为移动服务client连接成功，在这边处理业务自己的事件
        Log.i(TAG, "HuaweiApiClient 连接成功")
        requestToken()
    }

    override fun onConnectionSuspended(arg0: Int) {
        //HuaweiApiClient断开连接的时候，业务可以处理自己的事件
        Log.i(TAG, "HuaweiApiClient 连接断开")
        //HuaweiApiClient异常断开连接, if 括号里的条件可以根据需要修改
        if (isActivityExit) {
            client!!.connect()
        }
    }

    private fun setReceiveNotifyMsg(flag: Boolean) {
        if (client == null) return

        if (!client!!.isConnected) {
            Log.i(TAG, "设置是否接收push通知消息失败，原因：HuaweiApiClient未连接")
            client!!.connect()
            return
        }
        if (flag) {
            Log.i(TAG, "允许应用接收push通知栏消息")
        } else {
            Log.i(TAG, "禁止应用接收push通知栏消息")
        }
        com.huawei.hms.support.api.push.HuaweiPush.HuaweiPushApi.enableReceiveNotifyMsg(client, flag)
    }

    override fun onConnectionFailed(arg0: ConnectionResult) {
        Log.i(TAG, "HuaweiApiClient连接失败，错误码：" + arg0.errorCode)

        if (isActivityExit && HuaweiApiAvailability.getInstance().isUserResolvableError(arg0.errorCode)) {
            val errorCode = arg0.errorCode
            Handler(activity!!.get()?.getMainLooper()).post {
                // 此方法必须在主线程调用
                if (isActivityExit) {
                    HuaweiApiAvailability.getInstance().resolveError(activity!!.get(), errorCode, REQUEST_HMS_RESOLVE_ERROR)
                }
            }
        } else {
            //其他错误码请参见开发指南或者API文档
        }
    }


    /**
     * 当调用HuaweiApiAvailability.getInstance().resolveError方法的时候，会通过onActivityResult
     * 将实际处理结果返回给CP。
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == REQUEST_HMS_RESOLVE_ERROR) {
            if (resultCode == Activity.RESULT_OK) {

                val result = data?.getIntExtra(EXTRA_RESULT, 0)

                if (result == ConnectionResult.SUCCESS) {
                    Log.i(TAG, "错误成功解决")
                    if (client != null && !client!!.isConnecting && !client!!.isConnected) {
                        client!!.connect()
                    }
                } else if (result == ConnectionResult.CANCELED) {
                    Log.i(TAG, "解决错误过程被用户取消")
                } else if (result == ConnectionResult.INTERNAL_ERROR) {
                    Log.i(TAG, "发生内部错误，重试可以解决")
                    //CP可以在此处重试连接华为移动服务等操作，导致失败的原因可能是网络原因等
                } else {
                    Log.i(TAG, "未知返回码")
                }
            } else {
                Log.i(TAG, "调用解决方案发生错误")
            }

            return true
        }

        return false
    }

    fun deleteToken() {
        if (client == null) return

        if (!client!!.isConnected) {
            Log.i(TAG, "注销token失败，原因：HuaweiApiClient未连接")
            client!!.connect()
            return
        }

        //需要在子线程中执行删除token操作
        object : Thread() {
            override fun run() {
                //调用删除token需要传入通过getToken接口获取到token，并且需要对token进行非空判断
                Log.i(TAG, "删除Token：" + token)
                if (!TextUtils.isEmpty(token)) {
                    try {
                        com.huawei.hms.support.api.push.HuaweiPush.HuaweiPushApi.deleteToken(client, token)
                        token = ""
                    } catch (e: PushException) {
                        Log.i(TAG, "删除Token失败:" + e.message)
                    }

                }

            }
        }.start()
    }

}

