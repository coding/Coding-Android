package net.coding.program.common.util;

import android.os.Build;

import java.util.regex.Pattern;

/**
 * Created by zjh on 2017/2/22.
 */

public class PhoneUtil {

    public static boolean isFlyme() {
        return Build.FINGERPRINT.contains("Flyme") ||
                Pattern.compile("Flyme", Pattern.CASE_INSENSITIVE).matcher(Build.DISPLAY).find();
    }

    public static boolean isAndroidLOLLIPOP() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP;
    }
}
