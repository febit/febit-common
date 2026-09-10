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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AccessLevel;
import org.febit.lang.NanosClock;
import org.febit.lang.func.ThrowingRunnable;
import org.febit.lang.func.ThrowingSupplier;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Optional;

/**
 * A per-key rate limiter backed by Caffeine cache.
 *
 * <p>Conceptually, each key enters a cooldown window after every
 * action — any call for the same key within that window is silently
 * skipped.  This is useful for throttling duplicate callbacks,
 * limiting retry storms, or deduplicating idempotent requests.
 *
 * <p>It is safe for concurrent use: a per-key mutex serializes
 * callers of the same key.  This is in contrast to a plain cache
 * (which only stores values) or a global rate limiter (which limits
 * total throughput across all keys).
 *
 * <p>This is a best-effort local guard, not a distributed lock —
 * cache eviction resets the throttle, and the mutex is per-slot
 * rather than per-key (see "Understanding the cache" below).
 *
 * <h3>Quick start</h3>
 * <pre>{@code
 * var throttle = CaffeineThrottle.<String>create(
 *     Duration.ofMinutes(1),
 *     Caffeine.newBuilder().maximumSize(10_000)
 * );
 *
 * // throttle duplicate alerts — boolean tells you whether it ran
 * if (throttle.execute("alert-" + errorCode, () -> pagerDuty.send(errorCode))) {
 *     log.info("[Throttle] alert sent for {}", errorCode);
 * }
 *
 * // throttle expensive remote calls — empty Optional means skipped
 * throttle.supply(userId, () -> fetchProfileFromRemote(userId))
 *         .ifPresent(profile -> localCache.put(userId, profile));
 * }</pre>
 *
 * <h3>Choosing the right method</h3>
 * <dl>
 *   <dt>{@link #execute(Object, ThrowingRunnable)}</dt>
 *   <dd>Fire-and-forget.  Returns {@code true} if the action ran,
 *       {@code false} if skipped.  The return value is unambiguous.
 *   <dt>{@link #supply(Object, ThrowingSupplier)}</dt>
 *   <dd>When you need a return value.  Returns
 *       {@link Optional#empty()} if throttled.
 *       <b>Warning:</b> the supplier itself returning {@code null}
 *       also produces an empty Optional.  If you check
 *       {@link Optional#isEmpty()} to detect throttling,
 *       ensure the supplier never returns {@code null}.
 *   <dt>{@link #touch(Object)}</dt>
 *   <dd>Refresh the window for a key without acquiring the mutex —
 *       useful when you learn from an external source that the
 *       action just happened.
 *   <dt>{@link #reset(Object)}</dt>
 *   <dd>Clear throttle state and allow immediate re-pipeline.
 * </dl>
 *
 * <h3>Understanding the cache</h3>
 * <p>Throttle state lives in Caffeine cache entries,
 * so the cache policy directly affects correctness.
 * <ul>
 *   <li>If an entry is evicted before the window expires,
 *       the throttle resets — the effective window becomes shorter.
 *       Set expiry to outlast the window
 *       (e.g. {@code expireAfterAccess(window.multipliedBy(4))})
 *       and size the cache for your active key set.
 *   <li>The mutex is per-slot, not per-key.
 *       If a slot is evicted while an action is running,
 *       the next caller gets a new slot and enters without blocking.
 *       Normally harmless (the window still applies),
 *       but with aggressive eviction and long-running actions
 *       the same key may execute concurrently.
 * </ul>
 *
 * @param <K> key type with correct {@code equals}/{@code hashCode}
 */
@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CaffeineThrottle<K> {

    private final Cache<K, Slot> cache;
    private final long windowNanos;
    private final NanosClock clock;

    public static <K> CaffeineThrottle<K> create(Duration window, Caffeine<Object, Object> caffeine) {
        return create(window, caffeine, System::nanoTime);
    }

    public static <K> CaffeineThrottle<K> create(Duration window, Caffeine<Object, Object> caffeine, NanosClock clock) {
        var cache = caffeine.<K, Slot>build();
        return new CaffeineThrottle<>(cache, window.toNanos(), clock);
    }

    private boolean isThrottled(Slot slot) {
        var last = slot.lastNanos;
        return last != 0 && (clock.now() - last) < windowNanos;
    }

    /**
     * Run {@code action} if this key is not throttled, otherwise skip.
     * The window is marked even if the action throws — retries within
     * the window are skipped.
     *
     * @return {@code true} if the action ran, {@code false} if skipped
     */
    public <E extends Throwable> boolean execute(K key, ThrowingRunnable<E> action) throws E {
        var slot = cache.asMap().computeIfAbsent(key, v -> new Slot());
        synchronized (slot) {
            if (isThrottled(slot)) {
                return false;
            }
            try {
                action.run();
            } finally {
                slot.lastNanos = clock.now();
            }
        }
        return true;
    }

    /**
     * Call {@code supplier} if this key is not throttled, otherwise return
     * {@link Optional#empty()}.
     * The window is marked even if the supplier throws — retries within
     * the window are skipped.
     *
     * <p><b>Warning:</b> "throttled" and "supplier returned {@code null}"
     * both produce {@link Optional#empty()}.
     * If you use {@link Optional#isEmpty()} to detect throttling,
     * make sure the supplier never returns {@code null},
     * or switch to {@link #execute(Object, ThrowingRunnable)}.
     *
     * @return the value, or {@link Optional#empty()} if throttled
     */
    public <T, E extends Throwable> Optional<T> supply(K key, ThrowingSupplier<@Nullable T, E> supplier) throws E {
        var slot = cache.asMap().computeIfAbsent(key, v -> new Slot());
        synchronized (slot) {
            if (isThrottled(slot)) {
                return Optional.empty();
            }
            try {
                return Optional.ofNullable(supplier.get());
            } finally {
                slot.lastNanos = clock.now();
            }
        }
    }

    /**
     * Refresh the throttle window for this key.
     * Does not acquire the mutex.  No-op if the key was evicted.
     */
    public void touch(K key) {
        var slot = cache.getIfPresent(key);
        if (slot != null) {
            slot.lastNanos = clock.now();
        }
    }

    /**
     * Clear throttle state and allow immediate re-pipeline.
     */
    public void reset(K key) {
        cache.invalidate(key);
    }

    /**
     * Approximate number of tracked keys.
     */
    public long estimatedSize() {
        return cache.estimatedSize();
    }

    private static class Slot {
        volatile long lastNanos;
    }
}
