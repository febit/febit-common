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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import org.febit.lang.annotation.NonNullApi;
import org.febit.lang.func.ThrowingSupplier;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@UtilityClass
public class Polling {

    public static <T> Builder<T> create(ThrowingSupplier<T, ? extends Throwable> supplier) {
        return new Builder<T>()
                .supplier(supplier);
    }

    @lombok.Builder(
            builderClassName = "Builder",
            buildMethodName = "poll"
    )
    private static <T> CompletableFuture<Context<T>> poll0(
            @Nonnull @NonNull final ThrowingSupplier<T, ? extends Throwable> supplier,
            @Nonnull @NonNull final Function<Context<T>, Duration> dynamicDelay,
            @Nonnull @NonNull final Predicate<Context<T>> completeIf,
            @Nonnull @NonNull final Executor executor,
            @Nullable Instant timeoutAt,
            @Nullable Long timeoutInMillis,
            @Nullable Duration initialDelay,
            @Nullable Supplier<Instant> clock
    ) {
        if (clock == null) {
            clock = Instant::now;
        }
        if (initialDelay == null) {
            initialDelay = Duration.ZERO;
        }
        if (timeoutInMillis != null) {
            var timeoutInMillisAt = clock.get().plusMillis(timeoutInMillis);
            if (timeoutAt == null || timeoutAt.isAfter(timeoutInMillisAt)) {
                timeoutAt = timeoutInMillisAt;
            }
        }
        if (timeoutAt == null) {
            throw new IllegalArgumentException("timeout is required");
        }
        var context = ContextImpl.<T>builder()
                .timeoutAt(timeoutAt)
                .clock(clock)
                .build();
        var ctrl = new PollingTask<>(context, executor, supplier, completeIf, dynamicDelay);

        return ctrl.async(initialDelay)
                .thenCompose(ctrl::chain);
    }

    @NonNullApi
    public static class Builder<T> {

        public Builder<T> delay(Duration delay) {
            return dynamicDelay(ctx -> delay);
        }

        public Builder<T> delay(long delay, TimeUnit unit) {
            return delayInMillis(unit.toMillis(delay));
        }

        public Builder<T> delayInMillis(long delay) {
            return delay(Duration.ofMillis(delay));
        }

        public Builder<T> timeout(Duration timeout) {
            return timeoutInMillis(timeout.toMillis());
        }

        public Builder<T> timeout(long timeout, TimeUnit unit) {
            return timeoutInMillis(unit.toMillis(timeout));
        }

        public Builder<T> completeIfNoErrors() {
            return completeIf(ctx -> !ctx.hasError());
        }

        public Builder<T> completeIfHasResult() {
            return completeIf(Context::hasLastResult);
        }

        public Builder<T> completeIfReturns(T expected) {
            return completeIf(ctx ->
                    Objects.equals(ctx.lastResult(), expected)
            );
        }

        public Builder<T> completeIfReturnsTrue() {
            return completeIf(ctx -> Boolean.TRUE.equals(ctx.lastResult()));
        }
    }

    @NonNullApi
    public interface Context<T> {

        Instant now();

        Instant timeoutAt();

        long attempts();

        Duration lastDelay();

        @Nullable
        T lastResult();

        @Nullable
        Throwable lastError();

        /**
         * Get the result if completed.
         *
         * @return null if not completed, last result if completed
         */
        @Nullable
        default T get() {
            return isCompleted() ? lastResult() : null;
        }

        boolean isCompleted();

        boolean isTimeout();

        default boolean hasError() {
            return lastError() != null;
        }

        default boolean hasLastResult() {
            return lastResult() != null;
        }
    }

    @RequiredArgsConstructor
    private static class PollingTask<T> {
        private final ContextImpl<T> context;
        private final Executor executor;
        private final ThrowingSupplier<T, ? extends Throwable> supplier;
        private final Predicate<Context<T>> completeIf;
        private final Function<Context<T>, Duration> intervalCalculator;

        CompletableFuture<Context<T>> chain(Context<T> ctx) {
            if (ctx.isCompleted() || ctx.isTimeout()) {
                return CompletableFuture.completedFuture(ctx);
            }
            var delay = intervalCalculator.apply(context);
            return async(delay)
                    .thenCompose(this::chain);
        }

        CompletableFuture<Context<T>> async(Duration delay) {
            if (context.isCompleted() || context.isTimeout()) {
                return CompletableFuture.completedFuture(context);
            }
            context.lastDelay.set(delay);
            var delayMillis = delay.toMillis();

            var future = delayMillis <= 0
                    ? CompletableFuture.supplyAsync(this::exec, executor)
                    : CompletableFuture.supplyAsync(
                    this::exec,
                    CompletableFuture.delayedExecutor(delayMillis, TimeUnit.MILLISECONDS, executor)
            );
            return future;
        }

        /**
         * Run the action and check the result.
         */
        private Context<T> exec() {
            var ctx = context;
            var now = ctx.now();
            if (ctx.isCompleted() || ctx.isTimeout()) {
                return ctx;
            }
            if (!now.isBefore(ctx.timeoutAt())) {
                ctx.timeout.set(true);
                return ctx;
            }
            ctx.attempts.increment();
            try {
                T result = supplier.get();
                ctx.report(result, null);
            } catch (Throwable e) {
                ctx.report(null, e);
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    ctx.completed.set(true);
                    return ctx;
                }
            }
            ctx.completed.set(
                    completeIf.test(ctx)
            );
            return ctx;
        }

    }

    @Accessors(fluent = true)
    @lombok.Builder(
            builderClassName = "Builder"
    )
    private static class ContextImpl<T> implements Context<T> {
        @Getter
        private final Instant timeoutAt;
        private final Supplier<Instant> clock;

        private final LongAdder attempts = new LongAdder();
        private final AtomicBoolean completed = new AtomicBoolean(false);
        private final AtomicBoolean timeout = new AtomicBoolean(false);
        private final AtomicReference<Duration> lastDelay = new AtomicReference<>(Duration.ZERO);
        private final AtomicReference<T> lastResult = new AtomicReference<>();
        private final AtomicReference<Throwable> lastError = new AtomicReference<>();

        public void report(@Nullable T result, @Nullable Throwable error) {
            lastResult.set(result);
            lastError.set(error);
        }

        @Override
        public Instant now() {
            return clock.get();
        }

        @Override
        public long attempts() {
            return attempts.intValue();
        }

        @Override
        public boolean isCompleted() {
            return completed.get();
        }

        @Override
        public boolean isTimeout() {
            return timeout.get();
        }

        @Override
        public Duration lastDelay() {
            return lastDelay.get();
        }

        @Override
        public T lastResult() {
            return lastResult.get();
        }

        @Override
        public Throwable lastError() {
            return lastError.get();
        }
    }

}
