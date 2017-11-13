package net.coding.program.push;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.coding.program.push.huawei.HuaweiPush;
import net.coding.program.push.xiaomi.CommonPushClick;
import net.coding.program.push.xiaomi.EventPushToken;
import net.coding.program.push.xiaomi.EventUnbindToken;
import net.coding.program.push.xiaomi.PushAction;
import net.coding.program.push.xiaomi.XiaomiPush;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by chenchao on 2017/11/2.
 */

public final class CodingPush {

    private static CodingPush sPush;

    public static CodingPush instance() {
        if (sPush == null) sPush = new CodingPush();
        return sPush;
    }

    private CodingPush() {
    }

    private Context context;
    PushAction pushAction;
    boolean usePush; // 企业版不使用，用户在设置里面设置了不使用也不启用
    CommonPushClick clickPush;

    public void initApplication(Context context, CommonPushClick clickPushAction) {
        this.clickPush = clickPushAction;
        if (Rom.isEmui()) {
            Log.d(PushAction.TAG, "use huawei push");
            HuaweiPush.instance().initApplication(clickPushAction);
        } else {  // default device use xiaomi push
            if (pushAction == null) {
                pushAction = new XiaomiPush();
            }
            if (pushAction.init(context, clickPushAction)) {
                this.context = context;
            }
        }
    }

    public void onCreate(Activity context, String gk) {
        if (Rom.isEmui()) {
            this.context = context;
            HuaweiPush.instance().onCreate(context, gk);
        }
    }

    public void onDestroy() {
        if (Rom.isEmui()) {
            HuaweiPush.instance().onDestroy();
        }
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Rom.isEmui()) {
            return HuaweiPush.instance().onActivityResult(requestCode, resultCode, data);
        }

        return false;
    }

    public void bindGK(String gk) {
        if (Rom.isEmui()) {
            HuaweiPush.instance().requestToken();
        } else {
            if (context != null && pushAction != null) {
                pushAction.bindGK(context, gk);
            }
        }
        EventBus.getDefault().postSticky(new EventPushToken("", ""));
    }

    public void unbindGK(String gk) {
        if (Rom.isEmui()) {
            HuaweiPush.instance().deleteToken();
        } else {
            if (context != null && pushAction != null) {
                pushAction.unbindGK(context, gk);
            }
        }

        EventBus.getDefault().postSticky(new EventUnbindToken());
    }
}
