package net.coding.program.push.huawei;

import android.app.Activity;
import android.support.annotation.NonNull;

/**
 * Created by chenchao on 2017/11/6.
 */

public interface HuaweiPushAction {

    void onCreate(@NonNull Activity activity, @NonNull String gk, @NonNull HuaweiPushClick click);
    void onDestroy();
}
