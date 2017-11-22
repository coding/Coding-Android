package net.coding.program.push.xiaomi;

import android.content.Context;

/**
 * Created by chenchao on 2017/11/2.
 */

public interface PushAction {

    String TAG = "CodingPush";

    boolean init(Context context, CommonPushClick ClickPushActionName);

    void bindGK(Context context, String gk);

    void unbindGK(Context context, String gk);

}
