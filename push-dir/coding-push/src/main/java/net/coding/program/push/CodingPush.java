package net.coding.program.push;

import android.content.Context;
import android.util.Log;

import study.chenchao.push_xiaomi.PushAction;
import study.chenchao.push_xiaomi.XiaomiPush;

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

    public void init(Context context) {
        if (Rom.isMiui()) {
            if (pushAction == null) {
                pushAction = new XiaomiPush();
            }
            if (pushAction.init(context)) {
                this.context = context;
            }
        } else if (Rom.isEmui()) {
            Log.d(PushAction.TAG, "use huawei push");
        } else {
            Log.d(PushAction.TAG, "use other push");
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
