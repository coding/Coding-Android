package net.coding.program.push;

import android.content.Context;
import android.util.Log;

import net.coding.program.push.xiaomi.CommonPushClick;
import net.coding.program.push.xiaomi.PushAction;
import net.coding.program.push.xiaomi.XiaomiPush;

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

    public void init(Context context, CommonPushClick clickPushAction) {
        if (!Rom.isEmui()) {
            Log.d(PushAction.TAG, "use huawei push");

        } else  {  // default device use xiaomi push
            if (pushAction == null) {
                pushAction = new XiaomiPush();
            }
            if (pushAction.init(context, clickPushAction)) {
                this.context = context;
            }
        }
    }

    public void bindGK(String gk) {
        if (context != null && pushAction != null) {
            pushAction.bindGK(context, gk);
        }
    }

    public void unbindGK(String gk) {
        if (context != null && pushAction != null) {
            pushAction.unbindGK(context, gk);
        }
    }
}
