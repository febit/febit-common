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
package org.febit.common.caffeine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A test-friendly subclass of {@link DelayQueue} that works with {@link ControllableClock}.
 *
 * <p>Instead of blocking based on system time, it:
 * <ol>
 *   <li>Listens to clock changes via {@link ControllableClock#onChange(Runnable)}</li>
 *   <li>On each clock change, moves expired elements to an internal {@link BlockingQueue}</li>
 *   <li>{@link #take()} blocks on the internal queue (not system time)</li>
 * </ol>
 *
 * <p>This allows tests to control time precisely and advance it instantly.
 */
public class MockDelayQueue<T extends Delayed> extends DelayQueue<T> {

    private final ControllableClock clock;
    private final List<T> pending = new ArrayList<>();
    private final BlockingQueue<T> readyQueue = new LinkedBlockingQueue<>();

    // Exception to throw from take(), for testing consumer resilience
    private volatile RuntimeException takeException;

    public MockDelayQueue(ControllableClock clock) {
        this.clock = clock;
        this.clock.onChange(this::moveExpiredToReady);
    }

    /**
     * Set an exception to be thrown by the next {@link #take()} call.
     * Set to null to clear.
     */
    public void setTakeException(RuntimeException e) {
        this.takeException = e;
    }

    @Override
    public boolean offer(T element) {
        synchronized (this) {
            pending.add(element);
        }
        // Immediately check if expired (in case clock already past expiry)
        moveExpiredToReady();
        // Always return true (mimics DelayQueue.offer behavior)
        return true;
    }

    @lombok.NonNull
    @Override
    public T take() throws InterruptedException {
        var e = this.takeException;
        if (e != null) {
            throw e;
        }
        return readyQueue.take();
    }

    /**
     * Move expired elements from pending to readyQueue.
     * Called on every clock change.
     */
    private void moveExpiredToReady() {
        synchronized (this) {
            pending.removeIf(element -> {
                long delay = element.getDelay(TimeUnit.NANOSECONDS);
                if (delay <= 0) {
                    readyQueue.offer(element);
                    return true;
                }
                return false;
            });
        }
    }
}
