package thread;

import manager.UserRunnable;
import manager.UserThread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description: 用户线程
 * @author: zhuchuanji
 * @create: 2021-01-02 03:08
 */
public class UserThreadPoolExecutor {

    private static UserThreadPoolExecutor INSTANCE = new UserThreadPoolExecutor();

    private ThreadPoolExecutor userThreadPool = ThreadManager.createThreadPool();

    private static final int CONFIG_CORE_SIZE = 100;

    private static final int CONFIG_MAX_SIZE = 1000;

    private static final long KEEP_ALIVE_TIME = 15_000;

    private Map<Object, UserThread> usersThread = new HashMap<>();

    private UserThreadPoolExecutor() {
    }

    public static UserThreadPoolExecutor getInstance() {
        return INSTANCE;
    }


    public <T> void execute(UserRunnable<T> userRunnable) {
        UserThread userThread = usersThread.get(userRunnable.getUniqueName());
        if (userThread == null) {
            userThread = new UserThread();
            userThread.add(userRunnable);
            userThreadPool.execute(userThread);
        } else {
            userThread.add(userRunnable);
        }
    }
}
