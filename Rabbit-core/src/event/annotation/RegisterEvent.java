package event.annotation;

/**
 * @description: 注册事件
 * @author: zhuchuanji
 * @create: 2021-01-02 00:52
 */
public @interface RegisterEvent {

    String listener() default "listener";

}
