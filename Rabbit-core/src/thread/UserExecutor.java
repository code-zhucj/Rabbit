package thread;

/**
 * @author zhuchuanji
 */
public interface UserExecutor {

    /**
     * 提交任务
     *
     * @param userRunnable
     */
    void submit(UserRunnable userRunnable);
}
