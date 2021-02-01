package thread;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @description: 用户线程
 * @author: zhuchuanji
 * @create: 2021-01-02 03:08
 */
public class UserThreadPoolExecutor extends AbstractExecutorService implements UserExecutor {

    private final ReentrantLock mainLock = new ReentrantLock();

    private final Condition condition = mainLock.newCondition();

    private static final Set<Worker> workers = new HashSet<>();

    private static final Map<String, BlockingQueue<Runnable>> USERS_TASK = new ConcurrentHashMap<>();

    private final ThreadFactory THREAD_FACTORY = newFactory("UserThread", false);

    @Override
    public void shutdown() {

    }

    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void execute(Runnable command) {

    }

    @Override
    public void submit(UserTask userRunnable) {
        String userId = userRunnable.getUserId();
        BlockingQueue<Runnable> queue = USERS_TASK.computeIfAbsent(userId, v -> createQueueAndStartThread(userId));
        queue.offer(userRunnable);
    }

    private BlockingQueue<Runnable> createQueueAndStartThread(String userId) {
        BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>();
        Thread thread = THREAD_FACTORY.newThread(new Task(userId, queue));
        thread.start();
        return queue;
    }


    private ThreadFactory newFactory(String executorName, boolean daemon) {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Worker worker = new Worker(executorName, daemon, r);
                workers.add(worker);
                return worker;
            }
        };
    }

    private static class Task implements Runnable {

        private String id;

        private BlockingQueue<Runnable> queue;

        private Task(String id, BlockingQueue<Runnable> queue) {
            this.id = id;
            this.queue = queue;
        }

        @Override
        public void run() {
            Runnable task;
            while ((task = queue.poll()) != null) {
                System.out.println("执行任务id:" + id + "开始");
                task.run();
                System.out.println("执行任务id:" + id + "结束");
            }
        }
    }

    private static class Worker extends Thread {

        private Worker(String executorName, boolean daemon, Runnable runnable) {
            super(runnable);
            this.setDaemon(daemon);
            this.setName(executorName + "." + this.getId());
        }

        @Override
        public void run() {
            try {
                super.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }
}