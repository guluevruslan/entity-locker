package org.example.lock;

public interface EntityLocker<T> {

    void lock(T entity) throws InterruptedException;

    void unlock(T entity);
}
