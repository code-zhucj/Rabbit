package module;

/**
 * 模块接口
 */
public interface Module {

    /**
     * 初始化
     */
    void init();

    /**
     * 执行
     */
    void execute();

    /**
     * 销毁
     */
    void destroy();

    /**
     * 模块启动方法
     */
    default void start() {
        init();
        execute();
        destroy();
    }
}
