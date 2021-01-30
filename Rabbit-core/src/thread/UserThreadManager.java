package thread;

import module.Module;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 用户线程管理器
 *
 * @author zhuchuanji
 * @date 2021/1/27
 */
final class UserThreadManager implements UserExecutor, Module {

    private final int maxUser;

    private final int corePoolSize;

    private static long aliveTime = 60 * 60 * 1000;

    private ThreadPoolExecutor threadPoolExecutor;

    private Map<String, ThreadTask> usersTasks;

    private Runnable checkTask;

    public UserThreadManager(ThreadPoolExecutor threadPoolExecutor, int maxUser, int corePoolSize) {
        this.threadPoolExecutor = threadPoolExecutor;
        this.maxUser = maxUser;
        this.corePoolSize = corePoolSize;
    }

    @Override
    public void submit(UserRunnable userRunnable) {
        String userId = userRunnable.getUserId();
        ThreadTask threadTask = usersTasks.computeIfAbsent(userId, v -> this.newThreadTask());
        threadTask.queue.offer(userRunnable);
    }

    @Override
    public void init() {
        checkTask = new CheckTask(usersTasks);
        usersTasks = new ConcurrentHashMap<>((int) (maxUser / 0.75F) + 1, 0.75F, corePoolSize);
    }

    @Override
    public void execute() {
        checkTask.run();
    }

    @Override
    public void destroy() {

    }

    private ThreadTask newThreadTask() {
        return new ThreadTask(new LinkedBlockingQueue<>(), System.currentTimeMillis(), false);
    }

    private static class CheckTask implements Runnable {

        private Map<String, ThreadTask> usersTasks;

        private volatile boolean running = false;

        private CheckTask(Map<String, ThreadTask> usersTasks) {
            this.usersTasks = usersTasks;
        }

        @Override
        public void run() {
            while (isRunning()) {
                long currentTime = System.currentTimeMillis();
                Iterator<Map.Entry<String, ThreadTask>> iterator = usersTasks.entrySet().iterator();
                while (iterator.hasNext()) {
                    ThreadTask threadTask = iterator.next().getValue();
                    if (!threadTask.isOccupationStatus() &&
                            threadTask.queue.size() <= 0 &&
                            currentTime - threadTask.getTime() >= aliveTime) {
                        iterator.remove();
                    }
                }
            }
        }

        private boolean isRunning() {
            return running;
        }

        private synchronized boolean setRunning(boolean running) {
            return running;
        }
    }


    private static class ThreadTask implements Runnable {

        private BlockingQueue<Runnable> queue;

        private long time;

        private boolean occupationStatus;

        private ThreadTask(BlockingQueue<Runnable> queue, long time, boolean occupationStatus) {
            this.queue = queue;
            this.time = time;
            this.occupationStatus = occupationStatus;
        }

        public boolean isOccupationStatus() {
            return occupationStatus;
        }

        public synchronized void setOccupationStatus(boolean occupationStatus) {
            this.occupationStatus = occupationStatus;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        @Override
        public void run() {

        }
    }

}
