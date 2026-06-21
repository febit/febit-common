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

import org.febit.lang.func.ThrowingSupplier;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.febit.lang.util.Polling.Context;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class PollingTest {

    private static final Executor EXECUTOR = Executors.newCachedThreadPool();

    static <T> ThrowingSupplier<T, Throwable> single(T value) {
        return new ThrowingSupplier<>() {
            @Override
            public T get() throws Throwable {
                return value;
            }
        };
    }

    static <T> ThrowingSupplier<T, Throwable> queue(Iterable<T> values) {
        var iterator = values.iterator();
        return new ThrowingSupplier<>() {
            @Override
            public T get() {
                return iterator.next();
            }
        };
    }

    @Test
    void happy() throws ExecutionException, InterruptedException {
        var supplier = spy(single("Hello"));
        var fixedNow = Instant.now();
        var polling = Polling.create(supplier)
                .dynamicDelay(context -> Duration.ZERO)
                .completeIf(context -> true)
                .executor(EXECUTOR)
                .timeoutInMillis(Millis.HOUR)
                .clock(() -> fixedNow)
                .poll();

        var ctx = polling.get();
        assertEquals(fixedNow, ctx.now());
        assertEquals(fixedNow.plusMillis(Millis.HOUR), ctx.timeoutAt());

        assertTrue(ctx.isCompleted());
        assertFalse(ctx.hasError());
        assertTrue(ctx.hasLastResult());
        assertEquals(Duration.ZERO, ctx.lastDelay());
        assertEquals(1, ctx.attempts());
        assertEquals("Hello", ctx.get());
    }

    @Test
    void alwaysFalseUntilTimeout() throws ExecutionException, InterruptedException {
        assertThat(Polling.create(single("Hello"))
                .dynamicDelay(context -> Duration.ofMillis(1))
                .completeIf(ctx -> false)
                .executor(Runnable::run)
                .timeoutInMillis(Millis.SECOND)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(false, Context::isCompleted)
                .returns(true, Context::isTimeout)
                .returns(false, Context::hasError)
                .returns(true, Context::hasLastResult)
                .returns(null, Context::get)
                .returns("Hello", Context::lastResult)
                .returns(Duration.ofMillis(1), Context::lastDelay)
                .satisfies(ctx -> assertTrue(ctx.attempts() > 1));
    }

    @Test
    void completeIfHasResult() throws ExecutionException, InterruptedException {
        assertThat(Polling.create(queue(Arrays.asList(null, null, "hi", "there")))
                .delay(Duration.ofMillis(1))
                .timeoutInMillis(Millis.HOUR)
                .completeIfHasResult()
                .executor(Runnable::run)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(true, Context::isCompleted)
                .returns(false, Context::hasError)
                .returns(true, Context::hasLastResult)
                .returns("hi", Context::get)
                .returns("hi", Context::lastResult)
                .returns(3L, Context::attempts);
    }

    @Test
    void completeIfReturnsTrue() throws ExecutionException, InterruptedException {
        assertThat(Polling.create(queue(Arrays.asList("true", 1, false, true, true)))
                .delay(Duration.ofMillis(1))
                .timeoutInMillis(Millis.HOUR)
                .completeIfReturnsTrue()
                .executor(Runnable::run)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(true, Context::isCompleted)
                .returns(false, Context::hasError)
                .returns(true, Context::hasLastResult)
                .returns(true, Context::get)
                .returns(true, Context::lastResult)
                .returns(4L, Context::attempts);
    }

    @Test
    void dynamicDelay() throws ExecutionException, InterruptedException {
        var initDelay = Duration.ofMillis(3);

        var builder = Polling.create(single("Hello"))
                .initialDelay(initDelay)
                .dynamicDelay(context -> context.lastDelay().plusMillis(2))
                .completeIf(context -> context.attempts() == 1)
                .executor(Runnable::run)
                .timeoutInMillis(Millis.MINUTE);

        for (int i = 1; i <= 10; i++) {
            var count = i;
            assertThat(builder
                    .completeIf(context -> context.attempts() == count)
                    .poll()
                    .get()
            )
                    .isNotNull()
                    .returns(true, Context::isCompleted)
                    .returns(false, Context::hasError)
                    .returns(true, Context::hasLastResult)
                    .returns("Hello", Context::get)
                    .returns("Hello", Context::lastResult)
                    .returns(initDelay.plusMillis(2 * (count - 1)), Context::lastDelay)
                    .returns((long) count, Context::attempts);
        }

    }

    @Test
    void cancel() {
        var polling = Polling.create(single("Hello"))
                .initialDelay(Duration.ofMinutes(1))
                .delay(Duration.ofMinutes(1))
                .timeout(Duration.ofHours(1))
                .completeIf(ctx -> false)
                .executor(EXECUTOR)
                .poll();
        EXECUTOR.execute(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            polling.cancel(true);
        });
        assertThrows(CancellationException.class, polling::get);
    }

    @Test
    void timeout() {
        var builder = Polling.create(single("Hello"))
                .completeIf(ctx -> false)
                .timeout(Duration.ofHours(1))
                .executor(EXECUTOR);

        assertThrows(TimeoutException.class, () -> builder
                .delay(Duration.ofMinutes(1))
                .poll()
                .get(100, TimeUnit.MILLISECONDS)
        );

        assertThrows(TimeoutException.class, () -> builder
                .initialDelay(Duration.ofMinutes(1))
                .delay(Duration.ZERO)
                .timeout(Duration.ofHours(1))
                .poll()
                .get(100, TimeUnit.MILLISECONDS)
        );

        assertThrows(TimeoutException.class, () -> builder
                .initialDelay(Duration.ofMillis(10))
                .delay(Duration.ofMillis(10))
                .timeout(Duration.ofHours(1))
                .poll()
                .get(100, TimeUnit.MILLISECONDS)
        );

    }

    @Test
    void timeoutWithTimeUnit() throws ExecutionException, InterruptedException {
        var fixedNow = Instant.now();
        assertThat(Polling.create(single("ok"))
                .delay(Duration.ZERO)
                .timeout(1, TimeUnit.HOURS)
                .completeIf(ctx -> true)
                .executor(Runnable::run)
                .clock(() -> fixedNow)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(true, Context::isCompleted)
                .returns(fixedNow.plusMillis(Millis.HOUR), Context::timeoutAt);
    }

    @Test
    void completeIfNoErrors() throws ExecutionException, InterruptedException {
        // Succeeds immediately, no errors
        assertThat(Polling.create(single("ok"))
                .delay(Duration.ZERO)
                .timeoutInMillis(Millis.HOUR)
                .completeIfNoErrors()
                .executor(Runnable::run)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(true, Context::isCompleted)
                .returns(false, Context::hasError)
                .returns("ok", Context::get);
    }

    @Test
    void hasLastResultFalseForNullResult() throws ExecutionException, InterruptedException {
        var supplier = queue(Arrays.asList(null, null, "finally"));
        assertThat(Polling.create(supplier)
                .delay(Duration.ZERO)
                .timeoutInMillis(Millis.HOUR)
                .completeIfHasResult()
                .executor(Runnable::run)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(true, Context::isCompleted)
                .returns(false, Context::hasError)
                .returns(true, Context::hasLastResult)
                .returns(3L, Context::attempts);
    }

    // ──── Builder parameter validation ────

    @Test
    void noTimeoutThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                Polling.create(single("ok"))
                        .delay(Duration.ZERO)
                        .completeIf(ctx -> true)
                        .executor(Runnable::run)
                        .poll()
        );
    }

    @Test
    void missingDynamicDelayThrows() {
        assertThrows(NullPointerException.class, () ->
                Polling.create(single("ok"))
                        .completeIf(ctx -> true)
                        .executor(Runnable::run)
                        .timeoutInMillis(Millis.SECOND)
                        .poll()
        );
    }

    @Test
    void missingCompleteIfThrows() {
        assertThrows(NullPointerException.class, () ->
                Polling.create(single("ok"))
                        .delay(Duration.ZERO)
                        .executor(Runnable::run)
                        .timeoutInMillis(Millis.SECOND)
                        .poll()
        );
    }

    @Test
    void missingExecutorThrows() {
        assertThrows(NullPointerException.class, () ->
                Polling.create(single("ok"))
                        .delay(Duration.ZERO)
                        .completeIf(ctx -> true)
                        .timeoutInMillis(Millis.SECOND)
                        .poll()
        );
    }

    @Test
    void nullDynamicDelayThrows() {
        assertThrows(NullPointerException.class, () ->
                Polling.create(single("ok"))
                        .dynamicDelay(null)
                        .completeIf(ctx -> true)
                        .executor(Runnable::run)
                        .timeoutInMillis(Millis.SECOND)
                        .poll()
        );
    }

    @Test
    void nullCompleteIfThrows() {
        assertThrows(NullPointerException.class, () ->
                Polling.create(single("ok"))
                        .delay(Duration.ZERO)
                        .completeIf(null)
                        .executor(Runnable::run)
                        .timeoutInMillis(Millis.SECOND)
                        .poll()
        );
    }

    @Test
    void nullExecutorThrows() {
        assertThrows(NullPointerException.class, () ->
                Polling.create(single("ok"))
                        .delay(Duration.ZERO)
                        .completeIf(ctx -> true)
                        .executor(null)
                        .timeoutInMillis(Millis.SECOND)
                        .poll()
        );
    }

    @Test
    void nullTimeoutDurationThrows() {
        assertThrows(NullPointerException.class, () ->
                Polling.create(single("ok"))
                        .delay(Duration.ZERO)
                        .completeIf(ctx -> true)
                        .executor(Runnable::run)
                        .timeout((Duration) null)
        );
    }

    @Test
    void nullTimeoutTimeUnitThrows() {
        assertThrows(NullPointerException.class, () ->
                Polling.create(single("ok"))
                        .delay(Duration.ZERO)
                        .completeIf(ctx -> true)
                        .executor(Runnable::run)
                        .timeout(1, null)
        );
    }

    // ──── Timeout configuration edge cases ────

    @Test
    void timeoutAtDirect() throws ExecutionException, InterruptedException {
        var fixedNow = Instant.now();
        var timeoutAt = fixedNow.plusSeconds(60);
        assertThat(Polling.create(single("ok"))
                .delay(Duration.ZERO)
                .completeIf(ctx -> true)
                .executor(Runnable::run)
                .timeoutAt(timeoutAt)
                .clock(() -> fixedNow)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(true, Context::isCompleted)
                .returns(timeoutAt, Context::timeoutAt);
    }

    @Test
    void timeoutEarlierWins_timeoutAtEarlier() throws ExecutionException, InterruptedException {
        var fixedNow = Instant.now();
        var timeoutAt = fixedNow.plusMillis(Millis.SECOND);
        assertThat(Polling.create(single("ok"))
                .delay(Duration.ZERO)
                .completeIf(ctx -> true)
                .executor(Runnable::run)
                .timeoutAt(timeoutAt)
                .timeoutInMillis(Millis.HOUR)
                .clock(() -> fixedNow)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(timeoutAt, Context::timeoutAt);
    }

    @Test
    void timeoutEarlierWins_timeoutInMillisEarlier() throws ExecutionException, InterruptedException {
        var fixedNow = Instant.now();
        var timeoutAt = fixedNow.plusMillis(Millis.HOUR);
        assertThat(Polling.create(single("ok"))
                .delay(Duration.ZERO)
                .completeIf(ctx -> true)
                .executor(Runnable::run)
                .timeoutAt(timeoutAt)
                .timeoutInMillis(Millis.SECOND)
                .clock(() -> fixedNow)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(fixedNow.plusMillis(Millis.SECOND), Context::timeoutAt);
    }

    @Test
    void timeoutAtInPastImmediateTimeout() throws ExecutionException, InterruptedException {
        var fixedNow = Instant.now();
        var timeoutAt = fixedNow.minusMillis(1);
        assertThat(Polling.create(single("ok"))
                .delay(Duration.ofMinutes(1))
                .completeIf(ctx -> false)
                .executor(Runnable::run)
                .timeoutAt(timeoutAt)
                .clock(() -> fixedNow)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(true, Context::isTimeout)
                .returns(0L, Context::attempts);
    }

    @Test
    void clockDefaultsToSystemTime() throws ExecutionException, InterruptedException {
        // null clock → defaults to Instant::now
        var before = Instant.now();
        var result = Polling.create(single("ok"))
                .delay(Duration.ZERO)
                .completeIf(c -> true)
                .executor(Runnable::run)
                .timeoutInMillis(Millis.HOUR)
                .poll()
                .get();
        var after = Instant.now();
        assertNotNull(result.now());
        assertFalse(result.now().isBefore(before));
        assertFalse(result.now().isAfter(after.plusSeconds(1)));
    }

    // ──── CompleteIf edge cases ────

    @Test
    void completeIfReturnsNull() throws ExecutionException, InterruptedException {
        assertThat(Polling.create(queue(Arrays.asList("a", null, "b")))
                .delay(Duration.ZERO)
                .timeoutInMillis(Millis.HOUR)
                .completeIfReturns(null)
                .executor(Runnable::run)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(true, Context::isCompleted)
                .returns(null, Context::get)
                .returns(null, Context::lastResult)
                .returns(2L, Context::attempts);
    }

    // ──── Error handling ────

    @Test
    void supplierThrowsRuntimeException() throws ExecutionException, InterruptedException {
        var msg = "boom";
        var supplier = new ThrowingSupplier<Object, RuntimeException>() {
            @Override
            public Object get() {
                throw new RuntimeException(msg);
            }
        };
        assertThat(Polling.create(supplier)
                .delay(Duration.ZERO)
                .timeoutInMillis(Millis.HOUR)
                .completeIf(c -> c.hasError())
                .executor(Runnable::run)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(true, Context::isCompleted)
                .returns(true, Context::hasError)
                .satisfies(ctx -> {
                    assertNotNull(ctx.lastError());
                    assertEquals(msg, ctx.lastError().getMessage());
                });
    }

    @Test
    void supplierThrowsExceptionThenRecovers() throws ExecutionException, InterruptedException {
        var supplier = new ThrowingSupplier<>() {
            private int count = 0;

            @Override
            public String get() {
                count++;
                if (count == 1) {
                    throw new RuntimeException("fail");
                }
                return "recovered";
            }
        };
        assertThat(Polling.create(supplier)
                .delay(Duration.ZERO)
                .timeoutInMillis(Millis.HOUR)
                .completeIfHasResult()
                .executor(Runnable::run)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(true, Context::isCompleted)
                .returns(false, Context::hasError)
                .returns("recovered", Context::get)
                .returns(2L, Context::attempts);
    }

    // ──── Initial delay ────

    @Test
    void initialDelayReflectedInLastDelay() throws ExecutionException, InterruptedException {
        var initDelay = Duration.ofMillis(50);
        assertThat(Polling.create(single("ok"))
                .initialDelay(initDelay)
                .dynamicDelay(ctx -> Duration.ZERO)
                .completeIf(ctx -> true)
                .executor(Runnable::run)
                .timeoutInMillis(Millis.HOUR)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(initDelay, Context::lastDelay)
                .returns(1L, Context::attempts);
    }

    @Test
    void initialDelayNullDefaultsToZero() throws ExecutionException, InterruptedException {
        assertThat(Polling.create(single("ok"))
                .initialDelay(null)
                .dynamicDelay(ctx -> Duration.ofMillis(1))
                .completeIf(ctx -> true)
                .executor(Runnable::run)
                .timeoutInMillis(Millis.HOUR)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(Duration.ZERO, Context::lastDelay);
    }

    // ──── Runtime behavior ────

    @Test
    void interruptedExceptionDuringSupplierSetsCompleted() throws ExecutionException, InterruptedException {
        var supplier = new ThrowingSupplier<Object, InterruptedException>() {
            @Override
            public Object get() throws InterruptedException {
                throw new InterruptedException("interrupted");
            }
        };
        var result = Polling.create(supplier)
                .delay(Duration.ZERO)
                .timeoutInMillis(Millis.HOUR)
                .completeIf(ctx -> false)
                .executor(Runnable::run)
                .poll()
                .get();
        assertTrue(result.isCompleted());
        assertTrue(result.hasError());
        assertEquals(1, result.attempts());
    }

    @Test
    void untilFixedValue() throws Throwable {
        var supplier = spy(queue(List.of(1, 2, 3, 4, 5)));

        assertThat(Polling.create(supplier)
                .delay(Duration.ofMillis(1))
                .timeoutInMillis(Millis.HOUR)
                .completeIfReturns(5)
                .executor(Runnable::run)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(true, Context::isCompleted)
                .returns(false, Context::hasError)
                .returns(true, Context::hasLastResult)
                .returns(5, Context::get)
                .returns(5, Context::lastResult)
                .returns(5L, Context::attempts)
        ;
        verify(supplier, times(5)).get();

        supplier = spy(queue(List.of(2, 4, 6, 8, 10)));
        assertThat(Polling.create(supplier)
                .delay(Duration.ofMillis(1))
                .timeoutInMillis(Millis.HOUR)
                .completeIfReturns(2)
                .executor(Runnable::run)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(true, Context::isCompleted)
                .returns(false, Context::hasError)
                .returns(true, Context::hasLastResult)
                .returns(2, Context::get)
                .returns(2, Context::lastResult)
                .returns(1L, Context::attempts)
        ;
        verify(supplier, times(1)).get();

        supplier = spy(queue(List.of(2, 4, 6, 8, 10)));
        assertThat(Polling.create(supplier)
                .delay(Duration.ofMillis(1))
                .timeoutInMillis(Millis.HOUR)
                .completeIfReturns(6)
                .executor(Runnable::run)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(true, Context::isCompleted)
                .returns(false, Context::hasError)
                .returns(true, Context::hasLastResult)
                .returns(6, Context::get)
                .returns(6, Context::lastResult)
                .returns(3L, Context::attempts);
        verify(supplier, times(3)).get();

        supplier = spy(new ThrowingSupplier<>() {
            private final AtomicInteger count = new AtomicInteger(0);

            @Override
            public Integer get() {
                return count.addAndGet(1);
            }
        });
        assertThat(Polling.create(supplier)
                .delay(Duration.ofMillis(1))
                .timeoutInMillis(Millis.HOUR)
                .completeIfReturns(100)
                .executor(Runnable::run)
                .poll()
                .get()
        )
                .isNotNull()
                .returns(true, Context::isCompleted)
                .returns(false, Context::hasError)
                .returns(true, Context::hasLastResult)
                .returns(100, Context::get)
                .returns(100, Context::lastResult)
                .returns(100L, Context::attempts);
        verify(supplier, times(100)).get();
    }

}
