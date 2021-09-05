package org.example.lock;

public class ThreadUnsafeCounter {
    private static final int NUMBER_COUNT = 1000;
    private int count;

    public ThreadUnsafeCounter increment() {
        for (int i = 1; i <= NUMBER_COUNT; i++) {
            count = count + i;
        }
        return this;
    }

    public int getCount() {
        return count;
    }
}
