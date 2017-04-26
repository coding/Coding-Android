package net.coding.program.common;

import android.content.Context;
import android.content.res.Resources;

import net.coding.program.R;

/**
 * Created by chenchao on 2017/3/27.
 * 有些类不方便从资源文件获取颜色
 */

public class CodingColor {

    public static void init(Context context) {
        Resources r = context.getResources();
        fontGreen = r.getColor(R.color.font_green);
        fontYellow = r.getColor(R.color.font_yellow);
        divideLine = r.getColor(R.color.divide_line);

        fontWhite = r.getColor(R.color.font_white);
        font1 = r.getColor(R.color.font_1);
        font2 = r.getColor(R.color.font_2);
        font3 = r.getColor(R.color.font_3);
        font4 = r.getColor(R.color.font_4);
    }

    public static int fontGreen;
    public static int fontYellow;
    public static int divideLine;

    public static int fontWhite;
    public static int font1;
    public static int font2;
    public static int font3;
    public static int font4;
}
