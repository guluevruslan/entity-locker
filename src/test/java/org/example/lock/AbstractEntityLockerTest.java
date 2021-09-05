package org.example.lock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractEntityLockerTest {
    private static final int THREAD_COUNT = 1000;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
    }

    abstract <T> EntityLocker<T> locker();

    @Test
    void checkLockerForDifferentEntityIdTypes() throws InterruptedException {
        int counter = 0;

        final EntityLocker<String> stringLocker = locker();
        stringLocker.lock("test");
        counter++;
        stringLocker.unlock("test");

        assertEquals(1, counter);

        final EntityLocker<Integer> intLocker = locker();
        intLocker.lock(1);
        counter++;
        intLocker.unlock(1);

        assertEquals(2, counter);

        final EntityLocker<StubEntity> stubEntityLocker = locker();
        stubEntityLocker.lock(new StubEntity("test", 1));
        counter++;
        stubEntityLocker.unlock(new StubEntity("test", 1));

        assertEquals(3, counter);
    }

    @Test
    void checkOperationIsLockedIfOtherThreadUnlockedYet() throws InterruptedException {
        final EntityLocker<Integer> locker = locker();
        final Integer entityId = 1;

        locker.lock(entityId);

        final Future<?> anotherThreadOperation = executorService.submit(() -> {
            try {
                locker.lock(entityId);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                locker.unlock(entityId);
            }
        });

        final Executable operationExecution = () -> {
            try {
                anotherThreadOperation.get(1, TimeUnit.SECONDS);
            } finally {
                anotherThreadOperation.cancel(true);
                locker.unlock(entityId);
            }
        };

        assertThrows(TimeoutException.class, operationExecution);
    }

    @Test
    void checkForNoLockForDifferentEntityIds() throws InterruptedException, TimeoutException, ExecutionException {
        final EntityLocker<Integer> locker = locker();
        final Integer entityId = 1;

        locker.lock(entityId);

        final Future<Boolean> anotherThreadOperation = executorService.submit(() -> {
            final Integer anotherEntityId = 2;
            try {
                locker.lock(anotherEntityId);
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            } finally {
                locker.unlock(anotherEntityId);
            }
        });

        assertTrue(anotherThreadOperation.get(1, TimeUnit.SECONDS));

        locker.unlock(entityId);
    }

    @RepeatedTest(100)
    void checkForConcurrentSafe() throws ExecutionException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final List<Future<?>> results = new ArrayList<>();
        final EntityLocker<Integer> locker = MapEntityLocker.defaultLocker();

        final ThreadUnsafeCounter counter = new ThreadUnsafeCounter();
        final Integer entityId = 1;
        for (int i = 0; i < THREAD_COUNT; i++) {
            results.add(executorService.submit(
                    () -> {
                        try {
                            latch.await();
                            locker.lock(entityId);
                            counter.increment();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            locker.unlock(entityId);
                        }
                    }
            ));
        }

        latch.countDown();

        for (Future<?> result : results) {
            result.get();
        }

        final int oneIncrementResult = new ThreadUnsafeCounter().increment().getCount();
        final int expectedCount = oneIncrementResult * THREAD_COUNT;
        assertEquals(expectedCount, counter.getCount());
    }

    @Test
    void checkForReentant() throws InterruptedException {
        final EntityLocker<Integer> locker = locker();
        final Integer entityId = 1;

        int counter = 0;
        locker.lock(entityId);
        counter++;
        locker.lock(entityId);
        counter++;
        locker.lock(entityId);
        counter++;
        locker.unlock(entityId);
        locker.unlock(entityId);
        locker.unlock(entityId);

        assertEquals(3, counter);
    }
}