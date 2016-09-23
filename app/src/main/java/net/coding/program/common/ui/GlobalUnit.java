package net.coding.program.common.ui;

import net.coding.program.common.Global;

/**
 * Created by chenchao on 16/9/22.
 * 存放一些全局公用的 UI 字段
 */

public class GlobalUnit {

    public static void init() {
        ACTIONBAR_SHADOW = Global.dpToPx(2);
    }

    public static int ACTIONBAR_SHADOW;
}
