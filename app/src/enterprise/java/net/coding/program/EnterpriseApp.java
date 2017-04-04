package net.coding.program;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import net.coding.program.common.Global;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.compatible.EnterpriseCompatImp;
import net.coding.program.model.EnterpriseInfo;

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
        setHost(enterpriseName);

        EnterpriseInfo.instance().init(this);

        CodingCompat.init(new EnterpriseCompatImp());
    }

    public static void setHost(@NonNull String enterpriseName) {
        String host = "https://e.coding.net";

        if (Global.HOST.equals("t") || Global.HOST.startsWith("http://")) {
            if (TextUtils.isEmpty(enterpriseName)) {
                host = "http://e.staging.coding.test";
            } else {
                host = String.format("http://%s.staging.coding.test", enterpriseName);
            }
        } else {
            if (enterpriseName.isEmpty()) {
                host = "https://e.coding.net";
            } else {
                host = String.format("https://%s.coding.net", enterpriseName);
            }
        }

        setEnterpriseGK(enterpriseName);
        Global.HOST = host;
        Global.HOST_API = Global.HOST + "/api";
    }
}
