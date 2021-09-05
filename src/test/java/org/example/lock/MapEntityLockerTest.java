package org.example.lock;

class MapEntityLockerTest extends AbstractEntityLockerTest{

    @Override
    <T> EntityLocker<T> locker() {
        return MapEntityLocker.defaultLocker();
    }
}