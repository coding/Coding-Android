package net.coding.program;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.tencent.android.tpush.XGPushManager;

import net.coding.program.common.Global;
import net.coding.program.common.GlobalData;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.EnterpriseInfo;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.compatible.EnterpriseCompatImp;

/**
 * Created by cc191954 on 14-8-9.
 * 用来做一些初始化工作，比如设置 host，
 * 初始化图片库配置
 */
public class EnterpriseApp extends MyApp {

    @Override
    public void onCreate() {
        super.onCreate();

        EnterpriseInfo.instance().init(this);

        String enterpriseName = EnterpriseInfo.instance().getGlobalkey();
        if (enterpriseName.equalsIgnoreCase(PRIVATE_GK)) {
            initPrivateHost(this);
        } else {
            setHost(enterpriseName);
        }

        EnterpriseInfo.instance().init(this);

        CodingCompat.init(new EnterpriseCompatImp());

        Global.AUTHOR = "net.coding.program.enterprise.fileprovider";

        XGPushManager.registerPush(this);
    }

    public static void setHost(@NonNull String enterpriseName) {
        String host = "https://e.coding.net";
        AccountInfo.CustomHost customHost = AccountInfo.getCustomHost(GlobalData.getInstance());
        if (customHost.getHost().equalsIgnoreCase("s")) {
            if (TextUtils.isEmpty(enterpriseName)) {
                host = "http://e.coding.codingprod.net";
            } else {
                host = String.format("http://%s.coding.codingprod.net", enterpriseName);
            }
        } else {
            if (enterpriseName.isEmpty()) {
                host = "https://e.coding.net";
            } else {
                host = String.format("https://%s.coding.net", enterpriseName);
            }
        }

        GlobalData.setEnterpriseGK(enterpriseName);

//        host = "http://codingcorp.coding.com";
        Global.HOST = host;
        Global.HOST_API = Global.HOST + "/api";

        if (enterpriseName.contains("_")) {
            int start = host.indexOf("://") + "://".length();
            int end = host.indexOf(".");
            Global.HOST = Global.HOST.substring(0, start) + "e" + Global.HOST.substring(end, Global.HOST.length());
            Global.HOST_API = Global.HOST + "/api";
        }
    }

    private static void initPrivateHost(Context context) {
        GlobalData.setEnterpriseGK(PRIVATE_GK);
        Global.HOST = AccountInfo.loadLastPrivateHost(context);
        Global.HOST_API = Global.HOST + "/api";
    }

    public static void setPrivateHost(@NonNull String host) {
        String enterpriseGK = "";

        String lowerHost = host.toLowerCase();
        if (!lowerHost.startsWith("http://") && !lowerHost.startsWith("https://")) {
            host = String.format("http://%s", host);
        }

        int start = host.indexOf("//");
        int end = host.indexOf(".");
        if (start != -1 && end != -1) {
            enterpriseGK = host.substring(start + "//".length(), end);
        }

        GlobalData.setEnterpriseGK(PRIVATE_GK);
        Global.HOST = host;
        Global.HOST_API = Global.HOST + "/api";
    }

    public static String getEnterpriseGK(@NonNull String input) {
        String enterpriseGK = "";

        String lowerHost = input.toLowerCase();
        if (!lowerHost.startsWith("http://") && !lowerHost.startsWith("https://")) {
            input = String.format("http://%s", input);
        }

        int start = input.indexOf("//");
        int end = input.indexOf(".");
        if (start != -1 && end != -1) {
            enterpriseGK = input.substring(start + "//".length(), end);
        }

        return enterpriseGK;
    }


    public static final String PRIVATE_GK = "ce";
}
