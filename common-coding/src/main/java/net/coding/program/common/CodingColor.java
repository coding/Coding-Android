package net.coding.program.common;

import android.content.Context;
import android.content.res.Resources;

import net.coding.program.R;

/**
 * Created by chenchao on 2017/3/27.
 * 有些类不方便从资源文件获取颜色
 */

public class CodingColor {

    public static int fontGreen;
    public static int fontYellow;
    public static int divideLine;
    public static int fontWhite;
    public static int font1;
    public static int font2;
    public static int font3;
    public static int font4;
    public static int fontRed;
    public static int fontOrange;
    public static int fontBlue;
    public static int fontPink;
    public static int select1;
    public static int select2;
    public static int bg;
    public static String fontGreenString;
    public static String fontOrangeString;
    public static String fon1String;
    public static String fon2String;
    public static String fon3String;
    public static String fon4String;

    public static void init(Context context) {
        Resources r = context.getResources();
        fontGreen = r.getColor(R.color.font_green);
        fontYellow = r.getColor(R.color.font_yellow);
        divideLine = r.getColor(R.color.divide_line);
        bg = r.getColor(R.color.stand_bg);

        fontWhite = r.getColor(R.color.font_white);
        font1 = r.getColor(R.color.font_1);
        font2 = r.getColor(R.color.font_2);
        font3 = r.getColor(R.color.font_3);
        font4 = r.getColor(R.color.font_4);

        fon1String = Global.colorToString(font1);
        fon2String = Global.colorToString(font2);
        fon3String = Global.colorToString(font3);
        fon4String = Global.colorToString(font4);

        fontRed = r.getColor(R.color.font_red);
        fontBlue = r.getColor(R.color.font_blue);
        fontPink = r.getColor(R.color.font_pink);
        fontOrange = r.getColor(R.color.font_orange);

        fontGreenString = Global.colorToString(fontGreen);
        fontOrangeString = Global.colorToString(fontOrange);

        select1 = r.getColor(R.color.select_1);
        select2 = r.getColor(R.color.select_2);
    }
}
