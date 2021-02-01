package test;

import java.util.concurrent.locks.ReentrantLock;

public class User {

    public int id;

    public int count;

    public ReentrantLock lock = new ReentrantLock();

    public User(int id, int count) {
        this.id = id;
        this.count = count;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", count=" + count +
                '}';
    }
}
