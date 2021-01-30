package thread;

/**
 * 用户任务线程
 *
 * @author zhuchuanji
 */
public interface UserRunnable extends Runnable {

    /**
     * 获取用户id
     *
     * @return userId
     */
    String getUserId();
}
