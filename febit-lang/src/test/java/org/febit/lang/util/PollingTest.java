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
