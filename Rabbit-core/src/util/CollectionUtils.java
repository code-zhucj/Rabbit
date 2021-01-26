package util;

import java.util.Collection;

/**
 * @description: 集合工具类
 * @author: zhuchuanji
 * @create: 2021-01-02 01:24
 */
public class CollectionUtils {

    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }
}
