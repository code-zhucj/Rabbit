package event.annotation;

/**
 * @description: 注册监听器
 * @author: zhuchuanji
 * @create: 2021-01-02 00:50
 */
public @interface RegisterListener {

    String name() default "listener";

}
