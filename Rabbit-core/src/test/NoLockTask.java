package test;

import java.util.concurrent.Callable;

public class NoLockTask implements Runnable, Callable<Integer> {

    private User user;

    public NoLockTask(User user) {
        this.user = user;
    }

    public int getId(){
        return user.id;
    }

    @Override
    public void run() {
        Main.sleep();
        this.user.count++;
    }

    @Override
    public Integer call() throws Exception {
        run();
        return user.count;
    }
}
