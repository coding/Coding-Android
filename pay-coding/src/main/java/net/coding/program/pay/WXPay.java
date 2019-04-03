package net.coding.program.pay;

import android.content.Context;

import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by chenchao on 16/5/10.
 */
public final class WXPay {

    public void regToWeixin(Context context) {
        IWXAPI api = WXAPIFactory.createWXAPI(context, PayKeys.WX_APP_ID, false);
        api.registerApp(PayKeys.WX_APP_ID);
    }

    public void pay(String s) {

    }

    private static WXPay wxPay;

    private WXPay() {
    }

    public static WXPay getInstance() {
        if (wxPay == null) {
            wxPay = new WXPay();
        }

        return wxPay;
    }

}
