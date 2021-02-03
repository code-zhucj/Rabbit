package thread;

import java.util.concurrent.Future;

/**
 * 用户任务执行结果封装
 *
 * @author zhuchuanji
 */
public interface UserFutureTask<R> extends Runnable, Future<R> {

}
