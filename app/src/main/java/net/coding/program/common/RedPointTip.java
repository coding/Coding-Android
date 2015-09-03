package net.coding.program.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;

import net.coding.program.model.AccountInfo;

/**
 * Created by chenchao on 15/6/23.
 */
public class RedPointTip {

    private static final String SP_TIP_RED_POINT = "SP_TIP_RED_POINT";
    private static final String KEY_APP_LAST_VER = "KEY_APP_LAST_VER";

    public static boolean show(Context ctx, Type type) {
        SharedPreferences sp = ctx.getSharedPreferences(SP_TIP_RED_POINT, Context.MODE_PRIVATE);
        return !sp.getBoolean(type.name(), false);
    }

    public static void markUsed(Context ctx, Type type) {
        SharedPreferences.Editor edit = ctx.getSharedPreferences(SP_TIP_RED_POINT, Context.MODE_PRIVATE).edit();
        edit.putBoolean(type.name(), true);
        edit.commit();
    }

    public static int getLastVersion(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(SP_TIP_RED_POINT, Context.MODE_PRIVATE);
        return sp.getInt(KEY_APP_LAST_VER, 0);
    }

    public static void setLastVersion(Context ctx, int version) {
        SharedPreferences.Editor edit = ctx.getSharedPreferences(SP_TIP_RED_POINT, Context.MODE_PRIVATE).edit();
        edit.putInt(KEY_APP_LAST_VER, version);
        edit.commit();
    }

    public static void init(Context context) {
        int versionCode = 0;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = pInfo.versionCode;
        } catch (Exception e) {
            Global.errorLog(e);
        }

        int lastVersion = getLastVersion(context);
        if (lastVersion == 0 && !AccountInfo.isLogin(context)) { // 全新安装后第一次打开
            setLastVersion(context, versionCode);
            for (Type item : Type.values()) {
                markUsed(context, item);
            }

        } else if (lastVersion < versionCode) { // 升级安装后第一次打开
            setLastVersion(context, versionCode);
        }
    }

    public enum Type {
//        Task,
//        Code,
//        Readme,
//        Merge,
//        Pull,
//        CodeHistory,

//        Topic,
//        Task315,
//        MaopaoTopicSearch315,
//        MaopaoListSearch315,

        Voice320,
        File320,
        Merge320,
        MergeFile320
    }

}