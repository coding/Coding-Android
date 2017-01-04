package net.coding.program;

import net.coding.program.compatible.EnterpriseCompatImp;
import net.coding.program.compatible.CodingCompat;

/**
 * Created by cc191954 on 14-8-9.
 * 用来做一些初始化工作，比如设置 host，
 * 初始化图片库配置
 */
public class MyAppEnterprise extends MyApp {

    @Override
    public void onCreate() {
        super.onCreate();

        CodingCompat.init(new EnterpriseCompatImp());
    }
}
