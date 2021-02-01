package thread;

import module.Module;
import util.CollectionUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 用户线程管理器
 *
 * @author zhuchuanji
 * @date 2021/1/27
 */
public final class UserThreadManager implements UserExecutor, Module {

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
    public <R> void submit(UserTask<R> userTask) {
        String userId = userTask.getUserId();
        ThreadTask threadTask = usersTasks.computeIfAbsent(userId, v -> this.newThreadTask());
        threadTask.queue.offer(userTask);
    }

    @Override
    public void init() {
        checkTask = new CheckTask();
        usersTasks = new ConcurrentHashMap<>((int) (maxUser / 0.75F) + 1, 0.75F, corePoolSize);
    }

    @Override
    public void execute() {
        threadPoolExecutor.execute(checkTask);
    }

    @Override
    public void destroy() {

    }

    private ThreadTask newThreadTask() {
        return new ThreadTask(new LinkedBlockingQueue<>(), System.currentTimeMillis(), false);
    }

    private class CheckTask implements Runnable {

        private volatile boolean running = true;

        @Override
        public void run() {
            while (isRunning() || CollectionUtils.isNotEmpty(usersTasks)) {
                long currentTime = System.currentTimeMillis();
                Iterator<Map.Entry<String, ThreadTask>> iterator = usersTasks.entrySet().iterator();
                while (iterator.hasNext()) {
                    ThreadTask threadTask = iterator.next().getValue();
                    if (!threadTask.isOccupationStatus()) {
                        if (threadTask.queue.size() > 0) {
                            threadTask.setOccupationStatus(true);
                            threadTask.setTime(System.currentTimeMillis());
                            threadPoolExecutor.execute(threadTask);
                        } else if (currentTime - threadTask.getTime() >= aliveTime) {
                            iterator.remove();
                        }
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

        private BlockingQueue<UserTask<?>> queue;

        private long time;

        private boolean occupationStatus;

        private ThreadTask(BlockingQueue<UserTask<?>> queue, long time, boolean occupationStatus) {
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
            process();
            setOccupationStatus(false);
        }

        private void process() {
            UserTask<?> userTask;
            try {
                while ((userTask = queue.poll()) != null) {
                    userTask.run();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
