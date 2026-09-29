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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("resource")
class CaffeineDebouncerTest {

    private static final Duration DEBOUNCE = Duration.ofMillis(100);
    private static final Duration MAX_DEBOUNCE = Duration.ofMillis(500);
    private static final long BASE_NANOS = 1_000_000_000L;

    /**
     * Just past {@link #DEBOUNCE}: the moment a pending callback is due.
     */
    private static final Duration PAST_DEBOUNCE = DEBOUNCE.plusMillis(1);

    /**
     * Grace period for cases asserting that nothing fired.
     */
    private static final long SETTLE_MILLIS = 200;

    private static final long AWAIT_SECONDS = 2;

    private static final Runnable NOOP = () -> {
    };

    private static final ThreadFactory TEST_THREAD_FACTORY = r -> {
        var t = new Thread(r, "test-consumer");
        t.setDaemon(true);
        return t;
    };

    /**
     * Daemon for the same reason as {@link #TEST_THREAD_FACTORY}: a non-daemon executor
     * thread would outlive the suite and stop the JVM (and Gradle's cleanup of test
     * output) from exiting.
     */
    private static final ThreadFactory TEST_EXECUTOR_FACTORY = r -> {
        var t = new Thread(r, "test-executor");
        t.setDaemon(true);
        return t;
    };

    /**
     * Callbacks run on another thread, so a case has to wait for them; the timeout is
     * only a backstop for a failing case — a passing one never spends it.
     */
    static void assertFired(CountDownLatch latch) throws InterruptedException {
        assertThat(latch.await(AWAIT_SECONDS, TimeUnit.SECONDS)).isTrue();
    }

    /**
     * The recurring "advance just past the debounce window, then await the callback" tail
     * shared by most cases, so each case stays focused on what differs (the call sequence).
     */
    static void fireAfterDebounce(ControllableClock clock, CountDownLatch latch) throws InterruptedException {
        clock.advance(PAST_DEBOUNCE);
        assertFired(latch);
    }

    /**
     * The one creation entry: every case gets a {@link ControllableClock} that moves
     * only when the case advances it and a {@link MockDelayQueue} that surfaces expired
     * tasks without real waiting, so firing is asserted rather than slept for.
     */
    static DebouncerFixture debouncer(ControllableClock clock) {
        return new DebouncerFixture(clock);
    }

    /**
     * Build with the debouncer's own defaults: {@code config.validate()} must reject a
     * broken configuration before any executor, queue or consumer thread is involved.
     */
    static void assertRejects(
            CaffeineDebouncer.Configuration config,
            Class<? extends Throwable> type,
            String message
    ) {
        assertThatThrownBy(() -> CaffeineDebouncer.builder().config(config).build())
                .isInstanceOf(type)
                .hasMessageContaining(message);
    }

    /**
     * Debouncer under construction: defaults already cover the common case, so a case
     * overrides only the knob it exercises.
     */
    static final class DebouncerFixture {

        private final ControllableClock clock;
        private final MockDelayQueue<CaffeineDebouncer.Task<String>> queue;

        private Duration debounce = DEBOUNCE;
        private Duration tolerance = Duration.ZERO;
        @Nullable
        private Duration maxDebounce = MAX_DEBOUNCE;
        @Nullable
        private Long maxSize;
        private ExecutorService executor = Executors.newSingleThreadExecutor(TEST_EXECUTOR_FACTORY);
        private ThreadFactory threadFactory = TEST_THREAD_FACTORY;

        private DebouncerFixture(ControllableClock clock) {
            this.clock = clock;
            this.queue = new MockDelayQueue<>(clock);
        }

        DebouncerFixture debounce(Duration debounce) {
            this.debounce = debounce;
            return this;
        }

        DebouncerFixture tolerance(Duration tolerance) {
            this.tolerance = tolerance;
            return this;
        }

        DebouncerFixture maxDebounce(@Nullable Duration maxDebounce) {
            this.maxDebounce = maxDebounce;
            return this;
        }

        DebouncerFixture maxSize(long maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        DebouncerFixture executor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        DebouncerFixture threadFactory(ThreadFactory threadFactory) {
            this.threadFactory = threadFactory;
            return this;
        }

        MockDelayQueue<CaffeineDebouncer.Task<String>> queue() {
            return queue;
        }

        CaffeineDebouncer<String> build() {
            return CaffeineDebouncer.<String>builder()
                    .config(CaffeineDebouncer.Configuration.builder()
                            .debounce(debounce)
                            .tolerance(tolerance)
                            .maxDebounce(maxDebounce)
                            .maxSize(maxSize)
                            .threadFactory(threadFactory)
                            .build())
                    .executor(executor)
                    .delayQueue(queue)
                    .clock(clock)
                    .build();
        }
    }

    @Nested
    class ProcessLogic {

        @Test
        void firesCallbackWhenExpired() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            var latch = new CountDownLatch(1);
            d.call("key", latch::countDown);

            fireAfterDebounce(clock, latch);
        }

        @Test
        void onlyLastCallbackSurvives() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            var survives = new AtomicBoolean(false);
            d.call("key", () -> survives.set(false)); // discarded
            d.call("key", () -> survives.set(true));  // survives

            clock.advance(PAST_DEBOUNCE);

            var latch = new CountDownLatch(1);
            d.call("key", () -> {
                survives.set(true);
                latch.countDown();
            });

            clock.advance(PAST_DEBOUNCE);

            assertFired(latch);
            assertThat(survives.get()).isTrue();
        }
    }

    @Nested
    class Cancel {

        @Test
        void cancelsPendingCallback() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            var called = new AtomicBoolean(false);
            d.call("key", () -> called.set(true));

            d.cancel("key");

            clock.advance(PAST_DEBOUNCE);

            Thread.sleep(SETTLE_MILLIS);
            assertThat(called.get()).isFalse();
        }

        @Test
        void cancelIsNoOpForExecutedTask() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            var latch = new CountDownLatch(1);
            d.call("key", latch::countDown);

            fireAfterDebounce(clock, latch);

            assertThatCode(() -> d.cancel("key")).doesNotThrowAnyException();
        }

        @Test
        void cancelIsNoOpForNonExistentKey() {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            assertThatCode(() -> d.cancel("nonexistent")).doesNotThrowAnyException();
        }
    }

    @Nested
    class CallOverload {

        @Test
        void consumerReceivesTheKey() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            var received = new AtomicReference<String>();
            var latch = new CountDownLatch(1);
            // The Consumer<T> overload must forward the key to the callback.
            d.call("key", k -> {
                received.set(k);
                latch.countDown();
            });

            fireAfterDebounce(clock, latch);
            assertThat(received.get()).isEqualTo("key");
        }
    }

    @Nested
    class MaxDebounce {

        @Test
        void firesWhenMaxDebounceReached() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            var latch = new CountDownLatch(1);
            d.call("key", latch::countDown);

            // Keep renewing the window: only maxDebounce can end the wait
            var halfDebounce = DEBOUNCE.dividedBy(2);
            for (int i = 0; i < 10; i++) {
                clock.advance(halfDebounce);
                d.call("key", latch::countDown);
            }

            var remaining = MAX_DEBOUNCE.toNanos() - (clock.now() - BASE_NANOS);
            if (remaining > 0) {
                clock.advance(Duration.ofNanos(remaining + 1_000_000)); // +1ms
            }

            assertFired(latch);
        }
    }

    @Nested
    class Tolerance {

        /**
         * With {@code tolerance >= remaining delay}, a mid-window re-call does not push the
         * fire time out: the task fires at its original scheduled time instead of being
         * renewed for another debounce period.
         */
        @Test
        void firesWithoutRenewWhenWithinTolerance() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock)
                    .tolerance(DEBOUNCE)
                    .build();

            var latch = new CountDownLatch(1);

            // Scheduled to fire at t=DEBOUNCE (BASE + 100ms).
            d.call("key", latch::countDown);

            // Mid-window re-call would normally renew to now+DEBOUNCE (= BASE + 150ms),
            // but tolerance (100ms) >= remaining delay (50ms) skips the renew.
            clock.advance(DEBOUNCE.dividedBy(2));
            d.call("key", latch::countDown);

            // Fires at the original ~t=DEBOUNCE, not at the renewed t=150ms.
            clock.advance(DEBOUNCE.dividedBy(2).plusMillis(1));
            assertFired(latch);
        }
    }

    @Nested
    class Close {

        @Test
        void flushesAllPendingCallbacks() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            var latch = new CountDownLatch(2);
            d.call("key1", latch::countDown);
            d.call("key2", latch::countDown);

            d.close();
            assertFired(latch);
        }

        @Test
        void closeIsIdempotent() {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            d.close();

            assertThatCode(d::close).doesNotThrowAnyException();
        }
    }

    @Nested
    class EstimatedSize {

        @Test
        void tracksKeyCount() {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            assertThat(d.estimatedSize()).isEqualTo(0);

            d.call("key1", NOOP);
            assertThat(d.estimatedSize()).isEqualTo(1);

            d.call("key2", NOOP);
            assertThat(d.estimatedSize()).isEqualTo(2);

            // Same key should not increase count
            d.call("key1", NOOP);
            assertThat(d.estimatedSize()).isEqualTo(2);
        }

        @Test
        void decreasesAfterCancel() {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            d.call("key1", NOOP);
            d.call("key2", NOOP);
            assertThat(d.estimatedSize()).isEqualTo(2);

            d.cancel("key1");
            // Caffeine evicts asynchronously, so close (which invalidates all) settles the count
            d.close();
            assertThat(d.estimatedSize()).isEqualTo(0);
        }
    }

    @Nested
    class MultiKeyIsolation {

        @Test
        void differentKeysDoNotInterfere() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            d.call("key1", NOOP);
            d.call("key2", NOOP);

            clock.advance(PAST_DEBOUNCE);

            var latch1 = new CountDownLatch(1);
            var latch2 = new CountDownLatch(1);
            d.call("key1", latch1::countDown);
            d.call("key2", latch2::countDown);

            clock.advance(PAST_DEBOUNCE);

            assertFired(latch1);
            assertFired(latch2);
        }

        @Test
        void differentKeysHaveIndependentTimers() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            var key1Latch = new CountDownLatch(1);
            var key2Latch = new CountDownLatch(1);

            var halfDebounce = DEBOUNCE.dividedBy(2);

            // key1 called at t=0
            d.call("key1", key1Latch::countDown);

            // key2 called at t=debounce/2
            clock.advance(halfDebounce);
            d.call("key2", key2Latch::countDown);

            // At t=debounce+1ms, key1 should fire
            clock.advance(halfDebounce.plusMillis(1));
            assertFired(key1Latch);

            // key2 should not have fired yet
            assertThat(key2Latch.getCount()).isEqualTo(1);

            // At t=debounce+debounce/2+1ms, key2 should fire
            clock.advance(halfDebounce.plusMillis(1));
            assertFired(key2Latch);
        }
    }

    @Nested
    class DrainAndDrained {

        @Test
        void drainClearsActionAndMarksDrained() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            d.call("key", NOOP);

            // Cancel drains the task, so the next call has to build a fresh one
            d.cancel("key");
            d.call("key", NOOP);

            clock.advance(PAST_DEBOUNCE);

            var latch = new CountDownLatch(1);
            d.call("key", latch::countDown);

            fireAfterDebounce(clock, latch);
        }
    }

    @Nested
    class ConfigurationValidation {

        @Test
        void rejectsZeroDebounce() {
            var config = CaffeineDebouncer.Configuration.builder()
                    .debounce(Duration.ZERO)
                    .build();
            assertRejects(config, IllegalArgumentException.class, "debounce must be positive");
        }

        @Test
        void rejectsNegativeDebounce() {
            var config = CaffeineDebouncer.Configuration.builder()
                    .debounce(Duration.ofMillis(-1))
                    .build();
            assertRejects(config, IllegalArgumentException.class, "debounce must be positive");
        }

        @Test
        void rejectsMaxDebounceLessThanDebounce() {
            var config = CaffeineDebouncer.Configuration.builder()
                    .debounce(DEBOUNCE)
                    .maxDebounce(DEBOUNCE.dividedBy(2))
                    .build();
            assertRejects(config, IllegalArgumentException.class, "maxDebounce must be >=");
        }

        @Test
        void rejectsNegativeIdle() {
            var config = CaffeineDebouncer.Configuration.builder()
                    .debounce(DEBOUNCE)
                    .idle(Duration.ofMillis(-1))
                    .build();
            assertRejects(config, IllegalArgumentException.class, "idle must be positive");
        }

        @Test
        void rejectsNonPositiveMaxSize() {
            var config = CaffeineDebouncer.Configuration.builder()
                    .debounce(DEBOUNCE)
                    .maxSize(0L)
                    .build();
            assertRejects(config, IllegalArgumentException.class, "maxSize must be positive");
        }

        @Test
        void rejectsNegativeTolerance() {
            var config = CaffeineDebouncer.Configuration.builder()
                    .debounce(DEBOUNCE)
                    .tolerance(Duration.ofMillis(-1))
                    .build();
            assertRejects(config, IllegalArgumentException.class, "tolerance must be non-negative");
        }

        @Test
        void acceptsMinimalConfig() {
            assertThatCode(() -> CaffeineDebouncer.Configuration.builder()
                    .debounce(DEBOUNCE)
                    .build())
                    .doesNotThrowAnyException();
        }

        @Test
        void acceptsNullMaxDebounce() {
            assertThatCode(() -> CaffeineDebouncer.Configuration.builder()
                    .debounce(DEBOUNCE)
                    .maxDebounce(null)
                    .build())
                    .doesNotThrowAnyException();
        }

        @Test
        void ofBuildsMinimalConfig() {
            var config = CaffeineDebouncer.Configuration.of(DEBOUNCE, MAX_DEBOUNCE);
            assertThatCode(config::validate).doesNotThrowAnyException();
            assertThat(config.debounce()).isEqualTo(DEBOUNCE);
            assertThat(config.maxDebounce()).isEqualTo(MAX_DEBOUNCE);
        }

        @Test
        void rejectsThreadFactoryReturningNull() {
            var config = CaffeineDebouncer.Configuration.builder()
                    .debounce(DEBOUNCE)
                    .threadFactory(r -> null)
                    .build();
            assertRejects(config, IllegalStateException.class, "returned null");
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void maxDebounceEqualsDebounce() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock)
                    .debounce(DEBOUNCE)
                    .maxDebounce(DEBOUNCE)
                    .build();

            var latch = new CountDownLatch(1);
            d.call("key", latch::countDown);

            fireAfterDebounce(clock, latch);
        }

        @Test
        void verySmallDebounce() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var debounce = Duration.ofNanos(1);
            var d = debouncer(clock)
                    .debounce(debounce)
                    .maxDebounce(null)
                    .build();

            var latch = new CountDownLatch(1);
            d.call("key", latch::countDown);

            clock.advance(Duration.ofNanos(2));
            assertFired(latch);
        }

        @Test
        void toleranceLargerThanDebounce() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock)
                    .tolerance(DEBOUNCE.multipliedBy(2))
                    .build();

            var latch = new CountDownLatch(1);
            d.call("key", latch::countDown);

            // tolerance > debounce: any call within the debounce window will be skipped
            // the task should fire at the original scheduled time
            fireAfterDebounce(clock, latch);
        }
    }

    @Nested
    class CallbackException {

        @Test
        void consumerContinuesAfterCallbackThrows() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            var latch1 = new CountDownLatch(1);
            d.call("key1", () -> {
                latch1.countDown();
                throw new RuntimeException("expected test exception");
            });

            clock.advance(PAST_DEBOUNCE);
            assertFired(latch1);

            // Consumer should still be alive
            var latch2 = new CountDownLatch(1);
            d.call("key2", latch2::countDown);

            clock.advance(PAST_DEBOUNCE);
            assertFired(latch2);
        }
    }

    @Nested
    class CallAfterClose {

        @Test
        void doesNotCrash() {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            d.close();

            // Behavior after close is undefined, but it must not blow up in the caller's face
            assertThatCode(() -> d.call("key", NOOP))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    class ExecutorRejection {

        @Test
        void logsErrorAndContinuesOnRejection() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);

            var executor = Executors.newSingleThreadExecutor();
            var d = debouncer(clock)
                    .executor(executor)
                    .build();

            // Shut down executor to cause RejectedExecutionException
            executor.shutdownNow();

            var latch = new CountDownLatch(1);
            d.call("key", latch::countDown);

            clock.advance(PAST_DEBOUNCE);

            // Wait a bit - the callback won't fire, but the debouncer shouldn't crash
            Thread.sleep(SETTLE_MILLIS);
            assertThat(latch.getCount()).isEqualTo(1);
        }
    }

    @Nested
    class MaxSizeEviction {

        @Test
        void firesCallbackOnEviction() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock)
                    .maxSize(1)
                    .build();

            var latch = new CountDownLatch(1);
            d.call("key1", latch::countDown);

            // Pushing key2 in evicts key1 (maxSize = 1), which fires its callback
            d.call("key2", NOOP);

            assertFired(latch);
        }
    }

    @Nested
    class InvalidateAll {

        @Test
        void flushesWithoutClosing() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            var latch = new CountDownLatch(1);
            d.call("key", latch::countDown);

            // Invalidate without closing
            d.invalidateAll();

            assertFired(latch);

            // Debouncer should still be alive
            var latch2 = new CountDownLatch(1);
            d.call("key2", latch2::countDown);

            clock.advance(PAST_DEBOUNCE);
            assertFired(latch2);

            d.close();
        }

        @Test
        void clearsAllKeys() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            d.call("key1", NOOP);
            d.call("key2", NOOP);
            assertThat(d.estimatedSize()).isGreaterThan(0);

            d.invalidateAll();

            // Caffeine evicts asynchronously, so close settles the count
            d.close();
            assertThat(d.estimatedSize()).isEqualTo(0);
        }
    }

    @Nested
    class CallbackFiresExactlyOnce {

        @Test
        void callbackFiresExactlyOnce() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            // Only one pending callback ever exists per key; a re-call replaces (not
            // duplicates) it, so the callback must run exactly once.
            var firedCount = new AtomicInteger();
            d.call("key", firedCount::incrementAndGet);
            d.call("key", firedCount::incrementAndGet);

            clock.advance(PAST_DEBOUNCE);
            Thread.sleep(SETTLE_MILLIS);

            assertThat(firedCount.get()).isEqualTo(1);

            d.close();
        }
    }

    @Nested
    class ConsumerResilience {

        @Test
        void continuesAfterRuntimeExceptionInTake() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);

            var fixture = debouncer(clock);
            var d = fixture.build();

            // Make take() throw a RuntimeException
            fixture.queue().setTakeException(new RuntimeException("test exception"));

            // Wait a bit for the consumer to hit the exception and recover
            Thread.sleep(SETTLE_MILLIS);

            // Clear the exception so the consumer can continue
            fixture.queue().setTakeException(null);

            // Now the consumer should still be alive and processing
            var latch = new CountDownLatch(1);
            d.call("key", latch::countDown);

            fireAfterDebounce(clock, latch);

            d.close();
        }
    }

    @Nested
    class Concurrency {

        @Test
        void multipleCallsSameKeyFromMultipleThreads() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);
            var d = debouncer(clock).build();

            int threadCount = 4;
            int callsPerThread = 100;
            var latch = new CountDownLatch(1);
            var barrier = new CyclicBarrier(threadCount);
            var threads = new ArrayList<Thread>();

            for (int i = 0; i < threadCount; i++) {
                var t = new Thread(() -> {
                    try {
                        barrier.await();
                        for (int j = 0; j < callsPerThread; j++) {
                            d.call("key", latch::countDown);
                        }
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                });
                threads.add(t);
                t.start();
            }

            for (var t : threads) {
                t.join();
            }

            fireAfterDebounce(clock, latch);

            d.close();
        }
    }

    @Nested
    class VirtualThread {

        @Test
        void worksWithVirtualThreadConsumerAndExecutor() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                var d = debouncer(clock)
                        .threadFactory(Thread.ofVirtual().name("debouncer-consumer-", 0).factory())
                        .executor(executor)
                        .build();

                var latch = new CountDownLatch(1);
                d.call("key", latch::countDown);

                clock.advance(PAST_DEBOUNCE);
                assertFired(latch);

                d.close();
            }
        }

        @Test
        void worksWithPlatformConsumerAndVirtualThreadExecutor() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                var d = debouncer(clock)
                        .executor(executor)
                        .build();

                var latch = new CountDownLatch(3);
                d.call("key1", latch::countDown);
                d.call("key2", latch::countDown);
                d.call("key3", latch::countDown);

                clock.advance(PAST_DEBOUNCE);
                assertFired(latch);

                d.close();
            }
        }

        @Test
        void cancelWorksWithVirtualThreadExecutor() throws InterruptedException {
            var clock = new ControllableClock(BASE_NANOS);

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                var d = debouncer(clock)
                        .executor(executor)
                        .build();

                var called = new AtomicBoolean(false);
                d.call("key", () -> called.set(true));

                d.cancel("key");

                clock.advance(PAST_DEBOUNCE);
                Thread.sleep(SETTLE_MILLIS);
                assertThat(called.get()).isFalse();

                d.close();
            }
        }
    }
}
