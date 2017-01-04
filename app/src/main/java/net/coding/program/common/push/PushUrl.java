package net.coding.program.common.push;

import net.coding.program.common.Global;

/**
 * Created by chenchao on 16/1/18.
 */
public class PushUrl {
    public final String URL_2FA = getHost2FA();

    public static String getHost2FA() {
        return Global.HOST + "/app_intercept/show_2fa";
    }
}
