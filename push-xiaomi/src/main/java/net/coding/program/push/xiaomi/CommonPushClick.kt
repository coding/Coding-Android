package net.coding.program.push.xiaomi

import android.content.Context

/**
 * Created by chenchao on 2017/11/3.
 */

// 测试数据
//    1984
//   coding://coding.net/push/huawei?param_url=https%3a%2f%2fcoding.net%2fu%2f1984%2fp%2fauth%2ftask%2f1732723&notification_id=11
interface CommonPushClick {

    fun click(context: Context, params: Map<String, String>)
}
