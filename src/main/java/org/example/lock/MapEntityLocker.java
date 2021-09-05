package org.example.lock;

import org.example.entity.LockInfo;

import java.util.HashMap;
import java.util.Map;

public class MapEntityLocker<T> implements EntityLocker<T> {
    private final Map<T, LockInfo> locks;

    public MapEntityLocker(final Map<T, LockInfo> locks) {
        this.locks = locks;
    }

    public static <T> MapEntityLocker<T> defaultLocker() {
        return new MapEntityLocker<>(new HashMap<>());
    }

    @Override
    public void lock(final T entity) throws InterruptedException {
        synchronized (this) {
            while (locks.containsKey(entity) && locks.get(entity).isLockedByAnotherThread()) {
                wait();
            }

            locks.compute(entity, (k, v) -> computeLockInfo(entity));
        }
    }

    private LockInfo computeLockInfo(final T entity) {
        final LockInfo lockInfo = locks.get(entity);
        if (lockInfo == null) {
            return LockInfo.of();
        }

        return lockInfo.incrementAndGet();
    }

    @Override
    public void unlock(final T entity) {
        synchronized (this) {
            final LockInfo lockInfo = locks.get(entity);
            if (lockInfo != null && lockInfo.isLockedByCurrentThread()) {
                decreaseLockCountAndNotify(entity, lockInfo);
            }
        }
    }

    private void decreaseLockCountAndNotify(final T entity, final LockInfo lockInfo) {
        final LockInfo decreasedLockInfo = lockInfo.decrementAndGet();
        if (decreasedLockInfo.isLocked()) {
            locks.put(entity, decreasedLockInfo);
        } else {
            locks.remove(entity);
            notifyAll();
        }
    }
}
