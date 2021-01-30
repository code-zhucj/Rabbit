package manager;

import thread.UserThreadPoolExecutor;

/**
 * @description: 用户任务管理器
 * @author: zhuchuanji
 * @create: 2021-01-02 04:01
 */
public class UserTaskManager {

    private static final UserTaskManager userTaskManager = new UserTaskManager();

    private UserThreadPoolExecutor userThreadPoolExecutor = UserThreadPoolExecutor.getInstance();

    public static UserTaskManager getInstance() {
        return userTaskManager;
    }

    public <T> void execute(UserRunnable userRunnable) {
//        this.userThreadPoolExecutor.execute(userRunnable);
    }

    public <V> V submit() {
        return null;
    }
}
