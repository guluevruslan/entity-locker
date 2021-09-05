package org.example.lock;

import org.example.entity.Lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentMapEntityLocker<T> implements EntityLocker<T> {
    private final Map<T, Lock> locks;

    public ConcurrentMapEntityLocker(final Map<T, Lock> locks) {
        this.locks = locks;
    }

    public static <T> ConcurrentMapEntityLocker<T> defaultLocker() {
        return new ConcurrentMapEntityLocker<>(new ConcurrentHashMap<>());
    }

    @Override
    public void lock(final T entity) throws InterruptedException {
        for (; ; ) {
            final Lock lock = locks.computeIfAbsent(entity, k -> new Lock());

            lock.lock();

            if (lock == locks.get(entity)) {
                return;
            }

            lock.unlock();
        }
    }

    @Override
    public void unlock(final T entity) {
        final Lock lock = locks.get(entity);
        lock.unlock(() -> locks.remove(entity));
    }
}
