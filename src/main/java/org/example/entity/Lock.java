package org.example.entity;

public class Lock {
    private Thread thread;
    private int ownerThreadLocksCount;
    private int queuedThreadLocksCount;

    public synchronized void lock() throws InterruptedException {
        while (hasLocksByOwnerThread() && isLockRequestByNonOwnerThread()) {
            lockRequestByNonOwnerThread();
            threadToWaitStatus();
            tryToOwnLock();
        }
        lockByOwnerThread();
    }

    private void threadToWaitStatus() throws InterruptedException {
        try {
            wait();
        } catch (InterruptedException e) {
            queuedThreadLocksCount--;
            throw e;
        }
    }

    private boolean hasLocksByOwnerThread() {
        return ownerThreadLocksCount > 0;
    }

    private boolean isLockRequestByNonOwnerThread() {
        return !isLockedByCurrentThread();
    }

    private void lockRequestByNonOwnerThread() {
        queuedThreadLocksCount++;
    }

    private void tryToOwnLock() {
        queuedThreadLocksCount--;
    }

    private void lockByOwnerThread() {
        ownerThreadLocksCount++;
        thread = Thread.currentThread();
    }

    public synchronized void unlock(final Runnable unlockAction) {
        if (isLockedByCurrentThread()) {
            decreaseLockCountAndNotify(unlockAction);
        }
    }

    public void unlock() {
        unlock(() -> {});
    }

    private void decreaseLockCountAndNotify(final Runnable unlockAction) {
        unlockByOwnerThread();
        if (!hasLocksByOwnerThread()) {
            if (!hasQueuedLocks()) {
                unlockAction.run();
            }
            notifyAll();
        }
    }

    private void unlockByOwnerThread() {
        ownerThreadLocksCount--;
    }

    private boolean hasQueuedLocks() {
        return queuedThreadLocksCount > 0;
    }

    private boolean isLockedByCurrentThread() {
        return Thread.currentThread() == thread;
    }
}
