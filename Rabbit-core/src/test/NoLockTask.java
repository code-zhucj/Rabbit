package test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class NoLockTask implements Runnable {

    private User user;

    private List<NoLockTask> noLockTaskList;

    private CountDownLatch count;

    public NoLockTask(User user) {
        this.user = user;
    }

    public NoLockTask(List<NoLockTask> noLockTaskList, CountDownLatch count) {
        this.noLockTaskList = noLockTaskList;
        this.count = count;
    }

    @Override
    public void run() {
        noLockTaskList.forEach(NoLockTask::execute);
        count.countDown();
    }

    public void execute() {
        try {
            Thread.sleep(20);
            this.user.count++;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
