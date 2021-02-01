package thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 用户任务
 *
 * @author zhuchuanji
 */
public abstract class UserTask<V> implements RunnableFuture<V> {

    FutureTask<V> futureTask;

    public UserTask(Callable<V> callable) {
        this.futureTask = new FutureTask<>(callable);
    }

    public UserTask(Runnable runnable) {
        this.futureTask = new FutureTask<>(runnable, null);
    }

    public UserTask(Runnable runnable, V result) {
        this.futureTask = new FutureTask<>(runnable, result);
    }

    /**
     * 获取用户id
     *
     * @return userId
     */
    public abstract String getUserId();

    @Override
    public void run() {
        this.futureTask.run();
    }

    @Override
    public final V get() throws ExecutionException, InterruptedException {
        return this.futureTask.get();
    }

    @Override
    public final V get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        return this.futureTask.get(timeout, unit);
    }

    @Override
    public final boolean cancel(boolean mayInterruptIfRunning) {
        return this.futureTask.cancel(mayInterruptIfRunning);
    }

    @Override
    public final boolean isCancelled() {
        return this.futureTask.isCancelled();
    }

    @Override
    public final boolean isDone() {
        return this.futureTask.isDone();
    }
}
