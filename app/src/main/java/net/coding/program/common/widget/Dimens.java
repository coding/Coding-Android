package net.coding.program.common.widget;

import android.content.Context;

import net.coding.program.R;

/**
 * Created by chenchao on 15/5/11.
 * Application里面调用 initValue() 初始化
 */
public class Dimens {

    public static float PROJECT_ICON_ROUND = 2;

    public static void initValue(Context context) {
        PROJECT_ICON_ROUND = context.getResources().getDimension(R.dimen.project_icon_circle);
    }
}
