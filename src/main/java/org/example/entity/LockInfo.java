package org.example.entity;

public class LockInfo {
    private final Thread thread;
    private final int count;

    private LockInfo(final Thread thread, final int count) {
        this.thread = thread;
        this.count = count;
    }

    public static LockInfo of() {
        return new LockInfo(Thread.currentThread(), 1);
    }

    public LockInfo incrementAndGet() {
        return new LockInfo(thread, count + 1);
    }

    public LockInfo decrementAndGet() {
        return new LockInfo(thread, count - 1);
    }

    public boolean isLocked() {
        return count > 0;
    }

    public boolean isLockedByCurrentThread() {
        return Thread.currentThread().equals(thread);
    }

    public boolean isLockedByAnotherThread() {
        return !isLockedByCurrentThread();
    }
}
