package net.coding.program.common.util;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * @ClassName: DensityUtil
 * @Title:
 * @Description:(尺寸工具)
 * @Author:HouMingWei
 * @Since:2014-3-19下午5:16:19
 * @Version:1.0
 */
public class DensityUtil {

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @param fontScale （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * @param context
     * @return
     * @Title: screenWidth
     * @Description: (获取屏幕宽度)
     * @Author: houmingwei
     * @Since: 2014年8月31日下午3:02:35
     */
    public static int screenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int w = dm.widthPixels;
        return w;
    }

    /**
     * @param context
     * @return
     * @Title: screenWidth
     * @Description: (获取屏幕高度)
     * @Author: houmingwei
     * @Since: 2014年8月31日下午3:02:35
     */
    public static int screenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int h = dm.heightPixels;
        return h;
    }
}
