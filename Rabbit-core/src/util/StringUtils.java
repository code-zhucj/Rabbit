package util;

/**
 * @description: 字符串工具类
 * @author: zhuchuanji
 * @create: 2021-01-02 01:23
 */
public class StringUtils {

    private StringUtils() {

    }

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }

    public static boolean isBlank(String string) {
        return false;
    }

    public static boolean isNotBlank(String string) {
        return false;
    }
}
