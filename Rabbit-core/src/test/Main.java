package test;

import thread.ThreadManager;
import thread.UserTask;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class Main {

    private static final int MAX_USER = 100;

    private static final int MAX_TASK = 1000;

    public static void main(String[] args) {
        long lockCostTime = executeTime(Main::lockTask, createUsers());
        long noLockCostTime = executeTime(Main::noLockTask, createUsers());
        printResult(lockCostTime, noLockCostTime);
    }

    private static void noLockTask(Map<Integer, User> users) {
        List<UserTask<Boolean>> usersTasks = createUsersTasks(NoLockTask::new, users);
        usersTasks.forEach(v -> ThreadManager.USER.commit(v.getId(), v));
        ThreadManager.USER.shutdown();
        print(users);
    }

    private static void printResult(long lockCostTime, long noLockCostTime) {
        System.out.println();
        System.out.println("noLock 执行耗时：" + noLockCostTime + " ms");
        System.out.println("lock 执行耗时：" + lockCostTime + " ms");
        float percent =
                (float) ((noLockCostTime > lockCostTime ? (double) noLockCostTime / lockCostTime :
                        (double) lockCostTime / noLockCostTime) - 1D) * 100;
        String fast = noLockCostTime < lockCostTime ? "noLock" : "lock";
        String slow = noLockCostTime < lockCostTime ? "lock" : "noLock";
        System.out.println(fast + " 效率比 " + slow + " 提高了 " + percent + " %");
        System.out.println();
    }


    private static void lockTask(Map<Integer, User> users) {
        List<UserTask<Boolean>> usersTasks = createUsersTasks(LockTask::new, users);
        usersTasks.forEach(ThreadManager.SYSTEM::execute);
        ThreadManager.SYSTEM.shutdown();
        while (true) {
            if (ThreadManager.SYSTEM.isTerminated()) {
                break;
            }
        }
        print(users);
    }

    private static long executeTime(Consumer<Map<Integer, User>> consumer, Map<Integer, User> users) {
        long start = System.currentTimeMillis();
        consumer.accept(users);
        long end = System.currentTimeMillis();
        return end - start;
    }

    private static void print(Map<Integer, User> users) {
        users.values().forEach(v -> {
            if (v.count != MAX_TASK) {
                System.out.println(v);
            }
        });
    }

    private static List<UserTask<Boolean>> createUsersTasks(Function<User, Runnable> function, Map<Integer, User> users) {
        List<UserTask<Boolean>> userTasks = new LinkedList<>();
        for (int i = 0; i < MAX_USER; i++) {
            int id = i + 1;
            for (int j = 0; j < MAX_TASK; j++) {
                userTasks.add(new UserTask<Boolean>(id, function.apply(users.get(id))));
            }
        }
        return userTasks;
    }

    private static Map<Integer, User> createUsers() {
        Map<Integer, User> users = new HashMap<>();
        for (int i = 0; i < MAX_USER; i++) {
            users.put(i + 1, new User(i + 1, 0));
        }
        return users;
    }

    public static void sleep() {
        long sleep = 5;
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
