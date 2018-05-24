package net.coding.program.common.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;

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
        String regExp = "^[0-9]{5,14}$";
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

    public static boolean textValidate(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        Matcher matcher = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_-]+$").matcher(text);
        return matcher.find();
    }

    public static boolean checkEditIsFill(EditText... edits) {
        for (EditText edit : edits) {
            if (edit.getText().length() <= 0) {
                return false;
            }
        }

        return true;
    }
}
