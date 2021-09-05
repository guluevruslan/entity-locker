package org.example.lock;

class ConcurrentMapEntityLockerTest extends AbstractEntityLockerTest {

    @Override
    <T> EntityLocker<T> locker() {
        return ConcurrentMapEntityLocker.defaultLocker();
    }
}