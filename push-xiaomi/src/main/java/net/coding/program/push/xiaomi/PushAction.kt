package net.coding.program.push.xiaomi

import android.content.Context

/**
 * Created by chenchao on 2017/11/2.
 */

interface PushAction {

    fun init(context: Context, ClickPushActionName: CommonPushClick): Boolean

    fun bindGK(context: Context, gk: String)

    fun unbindGK(context: Context, gk: String)

    companion object {

        val TAG = "CodingPush"
    }

}
