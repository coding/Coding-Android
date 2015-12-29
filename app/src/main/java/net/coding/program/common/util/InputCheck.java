package net.coding.program.common.util;

import android.content.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenchao on 15/12/22.
 */
public class InputCheck {
    public static boolean isEmail(String s) {
        String regExp = "^.+@.+$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(s);
        return m.find();
    }

    public static boolean isPhone(String s) {
        String regExp = "^[0-9]{11}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(s);
        return m.find();
    }

    public static boolean checkEmail(Context context, String s) {
        boolean result = isEmail(s);
        if (!result) {
            SingleToast.showMiddleToast(context, "您输入的 Email 格式错误");
        }

        return result;
    }

    public static boolean checkPhone(Context context, String s) {
        boolean result = isPhone(s);
        if (!result) {
            SingleToast.showMiddleToast(context, "您输入的电话号码格式错误");
        }
        return result;
    }
}
