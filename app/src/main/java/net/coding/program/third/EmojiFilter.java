package net.coding.program.third;

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
}