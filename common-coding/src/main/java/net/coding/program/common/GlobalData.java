package net.coding.program.common;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import net.coding.program.common.model.UserObject;

/**
 * Created by chenchao on 2017/11/24.
 */

public class GlobalData {
    public static UserObject sUserObject;
    public static float sScale;
    public static int sWidthDp;
    public static int sWidthPix;
    public static int sHeightPix;
    public static int sEmojiNormal;
    public static int sEmojiMonkey;
    public static Unread sUnread;
    public static String enterpriseGK = "";
    public static Application app;
    private static int sMainCreate = 0;

    // 应对修改企业版路径以 /p/project 开头的问题
    public static String transformEnterpriseUri(String uri) {
        if (uri.startsWith("/p/")) {
            uri = String.format("/u/%s%s", getEnterpriseGK(), uri);
        } else if (uri.startsWith(Global.HOST + "/p/")) {
            int pathStart = Global.HOST.length();
            String uriPath = uri.substring(pathStart, uri.length());
            uri = String.format("/u/%s%s", getEnterpriseGK(), uriPath);
        }

        return uri;
    }

    public static String getEnterpriseGK() {
        return enterpriseGK;
    }

    public static void setEnterpriseGK(String enterpriseGK) {
        GlobalData.enterpriseGK = enterpriseGK;
    }

    public static boolean isEnterprise() {
        return !TextUtils.isEmpty(enterpriseGK);
    }

    public static Application getInstance() {
        return app;
    }

    public static boolean getMainActivityState() {
        return sMainCreate > 0;
    }

    public static void setMainActivityState(boolean create) {
        if (create) {
            ++sMainCreate;
        } else {
            --sMainCreate;
        }
        Log.d("", "showsss " + sMainCreate);
    }

    public static void lunchSetGKActivity(Context context) {
        compatCallback.lunchSetGKActivity(context);
    }

    public static void lunchLoginActivity(Context context) {
        compatCallback.lunchLoginActivity(context);
    }

    public static CompatCallback compatCallback;

    public interface CompatCallback {
        void lunchSetGKActivity(Context context);

        void lunchLoginActivity(Context context);
    }
}
