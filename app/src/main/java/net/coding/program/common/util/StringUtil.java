package net.coding.program.common.util;

/**
 * Created by zjh on 2017/2/18.
 * 字符串工具类
 */

public class StringUtil {
    /**
     * 判断字符串中是否包含该字符串，不包含大小写
     *
     * @param source 源字符
     * @param part   判断的字符
     * @return
     */
    public static boolean isExist(String source, String part) {
        source = source.toUpperCase();
        part = part.toUpperCase();
        return source.contains(part);
    }
}
