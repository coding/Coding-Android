package net.coding.program.third;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chaochen on 14-9-25.
 */
public class EmojiFilter {

    private final static Pattern mPattern = Pattern.compile("∀[^@\\s<>・：:，,。…~!！°？?'‘\"（）\\u0800-\\u9fa5^\\u0020-\\u007e\\s\\t\\n\\r\\n\\u3002\\uff1b\\\\uff0c\\\\uff1a\\\\u201c\\\\u201d\\\\uff08\\\\uff09\\\\u3001\\\\uff1f\\\\u300a\\\\u300b\\\\uff01\\\\u2019\\\\u2018\\\\u2026\\u2014\\uff5e\\uffe5]+");

    public static boolean containsEmoji(String source) {
        Matcher matcher = mPattern.matcher(source);
        return matcher.find();
    }

    public static boolean containsEmptyEmoji(Context context, String input, String alertEmpty, String alertEmoji) {
        if (input.isEmpty()) {
            showMiddleToast(context, alertEmpty);
            return true;
        }

        return containsEmoji(context, input, alertEmoji);
    }

    public static boolean containsEmptyEmoji(Context context, String input) {
        if (input.replaceAll(" ", "").replaceAll("　", "").isEmpty()) {
            showMiddleToast(context, "内容不能为空");
            return true;
        }

        return containsEmoji(context, input);
    }

    public static boolean containsEmoji(Context context, String input) {
        return containsEmoji(context, input, "暂不支持发表情");
    }

    public static boolean containsEmoji(Context context, String input, String alertMessage) {
        if (EmojiFilter.containsEmoji(input)) {
            showMiddleToast(context, alertMessage);
            return true;
        }

        return false;
    }

    private static void showMiddleToast(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}