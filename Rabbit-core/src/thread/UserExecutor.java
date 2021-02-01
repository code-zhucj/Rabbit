package thread;

/**
 * @author zhuchuanji
 */
public interface UserExecutor {

    /**
     * 提交任务
     *
     * @param userTask
     */
    <R> void submit(UserTask<R> userTask);
}
