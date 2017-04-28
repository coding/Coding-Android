package net.coding.program.common.ui;

import android.content.Context;

import net.coding.program.R;

/**
 * Created by chenchao on 16/9/22.
 * 存放一些全局公用的 UI 字段
 */

public class GlobalUnit {

    public static int ACTIONBAR_SHADOW;

    public static void init(Context context) {
        ACTIONBAR_SHADOW = context.getResources().getDimensionPixelSize(R.dimen.actionbar_shade);
    }
}
