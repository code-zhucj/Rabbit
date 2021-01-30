package thread;

import javafx.concurrent.Worker;
import manager.UserThread;
import module.Module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程管理器
 *
 * @author zhuchuanji
 */
public final class ThreadManager implements Module {

    protected static int corePoolSize;
    protected static int maximumPoolSize;
    protected static long keepAliveTime;
    protected static TimeUnit unit;
    protected static BlockingQueue<Runnable> workQueue;
    protected static ThreadFactory threadFactory;
    protected static RejectedExecutionHandler handler;


    private ThreadPoolExecutor threadPoolExecutor;

    public static ThreadPoolExecutor createThreadPool() {
        return null;
    }

    @Override
    public void init() {
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                threadFactory, handler);
        threadPoolExecutor.execute(() -> {
        });
//        new UserThreadExecutor("", new ArrayBlockingQueue<>(10)).start();
//        ThreadPoolSelector.USER.submit(() -> {
//        });
    }

    @Override
    public void execute() {

    }

    @Override
    public void destroy() {

    }

    public enum ThreadPoolSelector {
        /**
         * 用户线程
         */
//        USER(r -> UserThreadManager.getManager().execute(r)),
        /**
         * 系统线程
         */
        SYSTME(r -> {
        });
        private final Executor executor;

        ThreadPoolSelector(Executor executor) {
            this.executor = executor;
        }

        public void submit(Runnable r) {
            this.executor.execute(r);
        }
    }



}
