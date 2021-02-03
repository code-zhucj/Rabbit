package thread;

/**
 * @author zhuchuanji
 */
public interface UserExecutor {

    /**
     * 提交任务
     *
     * @param id       用户id
     * @param userTask 用户任务
     */
    <I, R> void submit(I id, UserFutureTask<R> userTask);
}
