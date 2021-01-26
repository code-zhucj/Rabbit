package manager;

/**
 * @description: 用户Runable接口
 * @author: zhuchuanji
 * @create: 2021-01-02 04:10
 */
public abstract class UserRunnable {

    private final String uniqueName;

    public UserRunnable(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    protected abstract void process();

    public String getUniqueName() {
        return uniqueName;
    }
}
