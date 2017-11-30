package net.coding.program.common.push;

import android.net.Uri;

import net.coding.program.common.Global;

/**
 * Created by chenchao on 16/1/18.
 */
public class PushUrl {

    public static boolean is2faLink(String link) {
        Uri uri = Uri.parse(link);
        return uri.getPath().equals("/app_intercept/show_2fa");
    }
}
