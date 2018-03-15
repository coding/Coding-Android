package net.coding.program;

import android.support.annotation.NonNull;
import android.text.TextUtils;

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
        setHost(enterpriseName);

        EnterpriseInfo.instance().init(this);

        CodingCompat.init(new EnterpriseCompatImp());

        Global.AUTHOR = "net.coding.program.enterprise.fileprovider";
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
        Global.HOST = host.toLowerCase();
        Global.HOST_API = Global.HOST + "/api";
    }
}
