package manager;

import java.util.LinkedList;

/**
 * @description: 用户线程
 * @author: zhuchuanji
 * @create: 2021-01-02 05:18
 */
public class UserThread implements Runnable {

    private LinkedList<UserRunnable> taskList;

    public UserThread() {
        this.taskList = new LinkedList<>();
    }

    @Override
    public void run() {
        UserRunnable task = this.taskList.pollFirst();
        if (task != null) {
            task.process();
        }
    }

    public void add(UserRunnable userRunnable) {
        taskList.addLast(userRunnable);
    }
}
