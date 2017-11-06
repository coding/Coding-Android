package net.coding.program.push.huawei;

import android.app.Activity;

/**
 * Created by chenchao on 2017/11/6.
 */

public interface HuaweiPushAction {

    void onCreate(Activity activity, String gk);
    void onDestroy();
}
