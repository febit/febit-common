/*
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.lang.util;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ExecutorUtilsTest {

    @Test
    void threadFactory_createsNonNullFactory() {
        var factory = ExecutorUtils.threadFactory("test-");
        assertNotNull(factory);
    }

    @Test
    void threadFactory_createsThreadsWithNamedPrefix() throws InterruptedException {
        var factory = ExecutorUtils.threadFactory("worker-");
        var holder = new AtomicReference<Thread>();
        var latch = new CountDownLatch(1);
        Thread t = factory.newThread(() -> {
            holder.set(Thread.currentThread());
            latch.countDown();
        });
        assertNotNull(t);
        assertTrue(t.getName().startsWith("worker-"), "thread name should start with prefix");
        t.start();
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(t, holder.get());
    }

    @Test
    void threadFactory_usesSequentialSuffix() {
        var factory = ExecutorUtils.threadFactory("seq-");
        var t1 = factory.newThread(() -> {
        });
        var t2 = factory.newThread(() -> {
        });
        var t3 = factory.newThread(() -> {
        });
        assertEquals("seq-1", t1.getName());
        assertEquals("seq-2", t2.getName());
        assertEquals("seq-3", t3.getName());
    }

    @Test
    void threadFactory_independentCounterPerInstance() {
        var a = ExecutorUtils.threadFactory("a-");
        var b = ExecutorUtils.threadFactory("b-");
        assertEquals("a-1", a.newThread(() -> {
        }).getName());
        assertEquals("b-1", b.newThread(() -> {
        }).getName());
    }

    @Test
    void threadFactory_threadStartsNotDaemon() {
        var factory = ExecutorUtils.threadFactory("d-");
        var t = factory.newThread(() -> {
        });
        assertFalse(t.isDaemon(), "default new Thread is not daemon");
    }

    private static ThreadPoolExecutor fullSingleWorkerExecutor() throws InterruptedException {
        var queue = new ArrayBlockingQueue<Runnable>(1);
        var keepBusy = new CountDownLatch(1);
        var exec = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, queue);
        exec.execute(keepBusy::countDown);
        assertTrue(keepBusy.await(1, TimeUnit.SECONDS));
        // Submit a task that blocks the worker indefinitely
        exec.execute(() -> {
            try {
                new CountDownLatch(1).await();
            } catch (InterruptedException ignored) {
            }
        });
        Thread.sleep(100);
        // Fill the queue to capacity
        assertTrue(queue.offer(() -> {
        }));
        return exec;
    }

    @Test
    void blockingRejected_offersTaskToQueue() throws Exception {
        // Spacious queue, single worker, task should run after enqueue
        var queue = new LinkedBlockingQueue<Runnable>(10);
        var factory = ExecutorUtils.threadFactory("ut-");
        var exec = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, queue, factory);
        // Prestart the worker thread: with capacity>0 queues, ThreadPoolExecutor
        // lazily creates workers, but our handler bypasses execute() and goes
        // straight to the queue.
        exec.prestartAllCoreThreads();
        var failed = new AtomicInteger();
        var handler = ExecutorUtils.blockingRejected(Duration.ofSeconds(2), r -> failed.incrementAndGet());

        var latch = new CountDownLatch(1);
        Runnable task = latch::countDown;
        handler.rejectedExecution(task, exec);

        // Wait for the queued task to be picked up and run
        assertTrue(latch.await(2, TimeUnit.SECONDS), "task should run");
        assertEquals(0, failed.get());
        exec.shutdown();
    }

    @Test
    void blockingRejected_callsOnFailed_whenQueueFull() throws Exception {
        var exec = fullSingleWorkerExecutor();
        var failed = new AtomicInteger();
        var handler = ExecutorUtils.blockingRejected(
                Duration.ofMillis(50),
                r -> failed.incrementAndGet()
        );
        handler.rejectedExecution(() -> {
        }, exec);

        assertEquals(1, failed.get(), "onFailed should be called after timeout");
        exec.shutdownNow();
    }

    @Test
    void blockingRejected_zeroTimeout_fails() throws Exception {
        var exec = fullSingleWorkerExecutor();
        var failed = new AtomicInteger();
        var handler = ExecutorUtils.blockingRejected(Duration.ZERO, r -> failed.incrementAndGet());
        handler.rejectedExecution(() -> {
        }, exec);

        assertEquals(1, failed.get());
        exec.shutdownNow();
    }

    @Test
    void blockingRejected_doesNotInvoke_onSuccess() throws Exception {
        var queue = new LinkedBlockingQueue<Runnable>();
        var factory = ExecutorUtils.threadFactory("ut-");
        var exec = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, queue, factory);
        exec.prestartAllCoreThreads();
        var failed = new AtomicInteger();
        var handler = ExecutorUtils.blockingRejected(Duration.ofSeconds(1), r -> failed.incrementAndGet());

        var latch = new CountDownLatch(1);
        handler.rejectedExecution(latch::countDown, exec);

        assertTrue(latch.await(2, TimeUnit.SECONDS), "task should run");
        assertEquals(0, failed.get());
        exec.shutdown();
    }

    @Test
    void blockingRejected_returnsHandlerInstance() {
        var handler = ExecutorUtils.blockingRejected(Duration.ofSeconds(1), r -> {
        });
        assertTrue(handler instanceof RejectedExecutionHandler);
    }

    @Test
    void blockingRejected_subsequentRejections_countUp() throws Exception {
        var exec = fullSingleWorkerExecutor();
        var failed = new AtomicInteger();
        var handler = ExecutorUtils.blockingRejected(Duration.ZERO, r -> failed.incrementAndGet());
        handler.rejectedExecution(() -> {
        }, exec);
        handler.rejectedExecution(() -> {
        }, exec);
        handler.rejectedExecution(() -> {
        }, exec);

        assertEquals(3, failed.get());
        exec.shutdownNow();
    }
}
