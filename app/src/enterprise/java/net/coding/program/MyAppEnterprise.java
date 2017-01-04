package net.coding.program;

import android.text.TextUtils;

import net.coding.program.common.Global;
import net.coding.program.compatible.CodingCompat;
import net.coding.program.compatible.EnterpriseCompatImp;
import net.coding.program.model.AccountInfo;

/**
 * Created by cc191954 on 14-8-9.
 * 用来做一些初始化工作，比如设置 host，
 * 初始化图片库配置
 */
public class MyAppEnterprise extends MyApp {

    @Override
    public void onCreate() {
        super.onCreate();

        String host = "http://e.coding.test";
        String enterpriseName = AccountInfo.getEnterpriseName(this);
        if (!TextUtils.isEmpty(enterpriseName)) {
            host = String.format("http://%s.coding.test", enterpriseName);
        }

        Global.HOST = host;
        Global.HOST_API = Global.HOST + "/api";

        CodingCompat.init(new EnterpriseCompatImp());
    }
}
