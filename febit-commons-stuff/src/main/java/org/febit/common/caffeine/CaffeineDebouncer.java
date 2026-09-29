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
import com.github.benmanes.caffeine.cache.RemovalListener;

import org.febit.lang.NanosClock;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * A per-key debouncer backed by Caffeine cache + DelayQueue.
 *
 * <p>Conceptually, each key has a rolling delay window — every call resets the
 * timer, and the action fires only after the window expires without further calls.
 * If the same key is called multiple times, <b>only the last callback is kept</b>;
 * all intermediate ones are discarded.  This is useful for coalescing rapid
 * updates (e.g., search-as-you-type, config hot-reload, UI refresh throttling).
 *
 * <p>It is safe for concurrent use: {@code cache.asMap().compute()} serializes
 * callers of the same key.  This is in contrast to a plain {@code DelayQueue}
 * (which lacks per-key deduplication) or a global debouncer (which cannot track
 * multiple keys independently).
 *
 * <p>This is a best-effort local debouncer, not a distributed lock —
 * cache eviction triggers early pipeline, and the debounce window is reset
 * on eviction.  See "Understanding the cache" below.
 *
 * <h3>Quick start</h3>
 * <pre>{@code
 * var debouncer = CaffeineDebouncer.builder()
 *     .debounce(Duration.ofMillis(300))       // reset window on each call
 *     .maxDebounce(Duration.ofSeconds(3))     // cap total wait (optional)
 *     .idle(Duration.ofMinutes(5))            // evict idle keys (optional)
 *     .maxSize(10_000L)                      // LRU cap (optional)
 *     .executor(executor)                    // where callbacks actually run
 *     .build();
 *
 * // coalesce rapid config changes — only the last write triggers a reload
 * debouncer.call("config-" + configKey, () -> reloadConfig(configKey));
 *
 * // search-as-you-type — only the latest query runs
 * debouncer.call("search-" + userId, () -> runSearch(userId, latestQuery));
 * }</pre>
 *
 * <h3>Choosing the right method</h3>
 * <dl>
 *   <dt>{@link #call(Object, Runnable)}</dt>
 *   <dd>Submit a callback for debounced pipeline.
 *       Only the last callback per key survives; earlier ones are discarded.
 *   <dt>{@link #cancel(Object)}</dt>
 *   <dd>Cancel a pending callback.  No-op if already fired or cancelled.
 *   <dt>{@link #estimatedSize()}</dt>
 *   <dd>Approximate number of tracked keys (for monitoring).
 *   <dt>{@link #close()}</dt>
 *   <dd>Shut down and flush all pending callbacks.  Implements {@link AutoCloseable}.
 * </dl>
 *
 * <h3>Understanding the cache</h3>
 * <p>Debounce state lives in Caffeine cache entries, so the cache policy
 * directly affects behavior.  There are four ways a pending callback can fire:
 * <ol>
 *   <li><b>Normal expiry</b> — the delay window elapses without renewal.
 *   <li><b>Max debounce</b> — {@code maxDebounce} caps total wait time
 *       (set to {@code null} for no cap).
 *   <li><b>LRU eviction</b> — {@code maxSize} forces eviction; the evicted
 *       callback fires immediately.
 *   <li><b>Idle eviction</b> — {@code idle} (via {@code expireAfterAccess})
 *       evicts keys not called for a while; the callback fires immediately.
 * </ol>
 *
 * <p>Because eviction triggers pipeline, set {@code idle} to at least
 * {@code maxDebounce.multipliedBy(2)} (if {@code maxDebounce} is set) to
 * avoid premature firing under normal conditions.
 *
 * <h3>Performance tuning</h3>
 * <p>{@code tolerance} (default {@code Duration.ZERO}) skips renew when the
 * remaining delay is smaller than the tolerance.  This reduces {@code DelayQueue}
 * re-enqueue overhead under high-frequency calls, at the cost of firing up to
 * {@code tolerance} earlier than the ideal time.  Set to {@code Duration.ZERO}
 * for strict debounce.
 *
 * @param <K> key type with correct {@code equals}/{@code hashCode}
 */
@Slf4j
@lombok.RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class CaffeineDebouncer<K> implements AutoCloseable {

    private static final AtomicLong TASK_SEQ = new AtomicLong(0);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Configuration config;
    private final Cache<K, Task<K>> cache;
    private final DelayQueue<Task<K>> delayQueue;
    private final ExecutorService executor;
    private final NanosClock clock;
    @Nullable
    private volatile Thread consumerThread;

    @FunctionalInterface
    public interface Clock extends NanosClock {
    }

    /**
     * Create a new {@link CaffeineDebouncer} builder.
     *
     * <p>Example:
     * <pre>{@code
     * var debouncer = CaffeineDebouncer.builder()
     *     .debounce(Duration.ofMillis(300))
     *     .maxDebounce(Duration.ofSeconds(3))
     *     .idle(Duration.ofMinutes(5))
     *     .maxSize(10_000L)
     *     .executor(executor)
     *     .build();
     * }</pre>
     *
     * @param <K> key type
     * @return a new builder
     */
    public static <K> Builder<K> builder() {
        return new Builder<>();
    }

    @lombok.Builder(
            builderClassName = "Builder"
    )
    private static <K> CaffeineDebouncer<K> createForBuilder(
            @Nullable Configuration config,
            @Nullable Duration debounce,
            @Nullable Duration maxDebounce,
            @Nullable Duration tolerance,
            @Nullable Duration idle,
            @Nullable Long maxSize,
            @Nullable Integer initCap,
            @Nullable ThreadFactory threadFactory,
            @Nullable ExecutorService executor,
            @Nullable DelayQueue<Task<K>> delayQueue,
            @Nullable NanosClock clock
    ) {
        var configBuilder = config == null
                ? Configuration.builder()
                : config.toBuilder();
        if (debounce != null) {
            configBuilder.debounce(debounce);
        }
        if (tolerance != null) {
            configBuilder.tolerance(tolerance);
        }
        if (maxDebounce != null) {
            configBuilder.maxDebounce(maxDebounce);
        }
        if (idle != null) {
            configBuilder.idle(idle);
        }
        if (maxSize != null) {
            configBuilder.maxSize(maxSize);
        }
        if (initCap != null) {
            configBuilder.initCap(initCap);
        }
        if (threadFactory != null) {
            configBuilder.threadFactory(threadFactory);
        }
        if (executor == null) {
            //noinspection resource
            executor = Executors.newVirtualThreadPerTaskExecutor();
        }
        if (delayQueue == null) {
            delayQueue = new DelayQueue<>();
        }
        if (clock == null) {
            clock = System::nanoTime;
        }
        return create(configBuilder.build(), executor, delayQueue, clock);
    }

    private static <K> CaffeineDebouncer<K> create(
            Configuration config,
            ExecutorService executor,
            DelayQueue<Task<K>> delayQueue,
            NanosClock clock
    ) {
        config.validate();

        var caffeine = Caffeine.newBuilder();
        if (config.maxSize() != null) {
            caffeine.maximumSize(config.maxSize());
        }
        if (config.initCap() != null) {
            caffeine.initialCapacity(config.initCap());
        }
        if (config.idle() != null) {
            caffeine.expireAfterAccess(config.idle().toNanos(), TimeUnit.NANOSECONDS);
        }
        caffeine.removalListener((RemovalListener<Object, Task<K>>) (key, task, cause) -> {
            if (task == null || task.drained()) {
                return;
            }
            var action = task.drain();
            if (action == null) {
                return;
            }
            try {
                executor.execute(action);
            } catch (RejectedExecutionException e) {
                log.error("[Debouncer] Failed to execute debounced action on eviction key={}", key, e);
            } catch (Exception e) {
                log.error("[Debouncer] Unexpected error submitting debounced action key={}", key, e);
            }
        });

        @SuppressWarnings("unchecked")
        var cache = (Cache<K, Task<K>>) (Cache<?, ?>) caffeine.build();
        var debouncer = new CaffeineDebouncer<>(config, cache, delayQueue, executor, clock);

        var thread = config.threadFactory().newThread(debouncer::consumeLoop);
        if (thread == null) {
            throw new IllegalStateException("threadFactory returned null");
        }
        debouncer.consumerThread = thread;
        thread.start();
        debouncer.started.set(true);
        return debouncer;
    }

    private static Thread defaultThreadFactory(Runnable r) {
        var t = new Thread(r, "debouncer-consumer");
        t.setDaemon(true);
        return t;
    }

    private void consumeLoop() {
        while (started.get() && !Thread.currentThread().isInterrupted()) {
            try {
                var task = delayQueue.take();
                process(task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("[Debouncer] Error in debouncer consumer loop", e);
            }
        }
    }

    /**
     * Process a task pulled from the {@link DelayQueue}.
     */
    void process(Task<K> target) {
        cache.asMap().compute(target.key(), (k, task) -> {
            if (task != target) {
                // target is stale (replaced by a newer task in cache), ignore it.
                // The newer task will be enqueued and processed separately.
                return task;
            }
            if (task.drained()) {
                return null; // Invalidated
            }

            long now = clock.now();

            if (task.tryRenew(now, config.tolerance().toNanos())) {
                // Renewed, re-enqueue for later pipeline
                delayQueue.offer(task);
                return task;
            }

            // Execute callback
            var action = task.drain();
            if (action != null) {
                try {
                    executor.execute(action);
                } catch (RejectedExecutionException e) {
                    log.error("[Debouncer] Failed to execute task key={}", task.key(), e);
                } catch (Exception e) {
                    log.error("[Debouncer] Unexpected error executing task key={}", task.key(), e);
                }
            }
            return null; // Invalidated
        });
    }

    public <T extends K> void call(T key, @lombok.NonNull Consumer<T> action) {
        call(key, () -> action.accept(key));
    }

    /**
     * Submit a callback for debounced pipeline.
     *
     * <p>If this key already has a pending callback, the old one is discarded
     * and the new one replaces it.  The delay window is reset to
     * {@code debounce}, bounded by {@code maxDebounce} (if set).
     *
     * @param key    business-unique identifier (e.g., {@code "config-" + configKey})
     * @param action callback to execute; must not be null
     */
    public void call(K key, @lombok.NonNull Runnable action) {
        long now = clock.now();
        cache.asMap().compute(key, (k, old) -> {
            if (old != null && !old.drained()) {
                // Reuse task, overwrite action, update renew time
                old.reset(action, now, config);
                return old;
            }
            // Create new task
            var at = Math.addExact(now, config.debounce().toNanos());
            var maxAt = config.maxDebounce() != null
                    ? Math.addExact(now, config.maxDebounce().toNanos())
                    : Long.MAX_VALUE;
            var task = Task.<K>builder()
                    .seq(TASK_SEQ.getAndIncrement())
                    .maxAt(maxAt)
                    .key(k)
                    .at(at)
                    .renewTo(at)
                    .action(action)
                    .clock(clock)
                    .build();
            delayQueue.offer(task);
            return task;
        });
    }

    /**
     * Cancel a pending callback for this key.
     *
     * <p>If the callback has already fired or been cancelled, this is a no-op.
     * The cancellation is atomic per key.
     *
     * @param key business-unique identifier
     */
    public void cancel(K key) {
        cache.asMap().compute(key, (k, oldTask) -> {
            if (oldTask != null) {
                oldTask.cancel();
            }
            return null;
        });
    }

    /**
     * Approximate number of currently tracked keys.
     *
     * <p>Useful for monitoring or debugging (e.g., alert if the debouncer
     * is growing unboundedly).  This is a snapshot, not a real-time count.
     *
     * @return approximate key count
     */
    public long estimatedSize() {
        return cache.estimatedSize();
    }

    /**
     * Shut down this debouncer and flush all pending callbacks.
     *
     * <p>After calling this method:
     * <ul>
     *   <li>The consumer thread stops processing new delays.
     *   <li>{@code invalidateAll()} triggers eviction for all entries,
     *       causing every pending callback to fire immediately.
     *   <li>Subsequent calls to {@link #call(Object, Runnable)} may not
     *       behave correctly (the consumer thread is stopped).
     * </ul>
     *
     * <p>Implements {@link AutoCloseable} for use with try-with-resources.
     */
    @Override
    public void close() {
        if (!started.compareAndSet(true, false)) {
            return;
        }
        var thread = consumerThread;
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        invalidateAll();
    }

    /**
     * Invalidate all keys, causing every pending callback to fire immediately.
     *
     * <p>Called automatically by {@link #close()}.  Useful if you want to
     * flush without shutting down (e.g., on config reload).
     */
    public void invalidateAll() {
        cache.invalidateAll();
    }

    /**
     * A debounce task tracked in the cache and scheduled in the DelayQueue.
     *
     * <p><b>Thread safety:</b> all mutations happen inside
     * {@code cache.asMap().compute()}, which serializes per key.
     * {@code at} and {@code drained} are {@code volatile} for lock-free
     * reads by {@link #getDelay(TimeUnit)} (called by the consumer thread).
     *
     * <p><b>Lifecycle:</b>
     * <ol>
     *   <li>Created by {@link #call(Object, Runnable)} — enqueued in
     *       {@code DelayQueue} with initial delay {@code debounce}.
     *   <li>On expiry, {@link #tryRenew(long, long)} may push the delay
     *       forward (re-enqueue); or the callback fires.
     *   <li>After firing, {@link #drain()} clears the callback and marks
     *       the task drained — it will be removed from the cache.
     * </ol>
     */
    @lombok.Getter
    @Accessors(fluent = true)
    @lombok.Builder(access = lombok.AccessLevel.PACKAGE)
    public static class Task<K> implements Delayed {
        private final long seq;
        private final long maxAt;
        private final K key;
        private final NanosClock clock;

        private volatile long at;
        private volatile long renewTo;

        private volatile boolean drained;
        @Nullable
        private volatile Runnable action;

        /**
         * Replace the callback and compute the next renew time.
         *
         * <p>Called by {@link #call(Object, Runnable)} when a key is
         * re-submitted.  The new {@code renewTo} is
         * {@code now + debounce}, capped at {@code maxAt}.
         *
         * @param action new callback (overwrites any pending one)
         * @param now    current monotonic time (nanos)
         * @param config configuration (used to read {@code debounce})
         */
        void reset(Runnable action, long now, Configuration config) {
            this.action = action;
            long candidate = Math.addExact(now, config.debounce().toNanos());
            this.renewTo = Math.min(candidate, this.maxAt);
        }

        /**
         * Attempt to push the firing time forward (re-enqueue).
         *
         * <p>Returns {@code false} (don't renew) if:
         * <ul>
         *   <li>{@code renewTo} has already passed (expired).
         *   <li>Remaining delay is less than {@code toleranceNanos}
         *       (too close to fire; skip micro-renew).
         * </ul>
         *
         * <p>Otherwise, copies {@code renewTo} to {@code at} and returns
         * {@code true} — the caller should re-enqueue this task.
         *
         * @param now            current monotonic time (nanos)
         * @param toleranceNanos skip renew if remaining delay &lt; this value
         * @return {@code true} if renewed (caller should re-enqueue),
         * {@code false} if the task should fire now
         */
        boolean tryRenew(long now, long toleranceNanos) {
            // Guard 1: already expired
            if (renewTo <= now) {
                return false;
            }
            // Guard 2: too close to fire, skip renew
            if (renewTo - now < toleranceNanos) {
                return false;
            }
            this.at = this.renewTo;
            return true;
        }

        /**
         * Cancel this task: drain the callback and mark as drained.
         * The task will be removed from the cache on the next {@code compute()}.
         *
         * <p>Must be called inside {@code cache.asMap().compute()}.
         */
        void cancel() {
            this.drain();
        }

        /**
         * Atomically clear the callback and mark this task as drained.
         *
         * <p>After this call, the task is inert: it will not fire,
         * and {@link #tryRenew(long, long)} will not renew it.
         * The task is removed from the cache on the next {@code compute()}.
         *
         * <p>Must be called inside {@code cache.asMap().compute()}.
         *
         * @return the callback before draining (may be {@code null})
         */
        @Nullable
        Runnable drain() {
            var fn = this.action;
            this.action = null;
            this.drained = true;
            return fn;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long delay = at - clock.now();
            return unit.convert(delay, TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            if (other == this) {
                return 0;
            }
            var that = (Task<?>) other;
            int cmp = Long.compare(this.at, that.at);
            if (cmp != 0) {
                return cmp;
            }
            return Long.compare(this.seq, that.seq);
        }
    }

    /**
     * Configuration for {@link CaffeineDebouncer}.
     *
     * <p>Only {@code debounce} is required; all other fields are optional
     * (default {@code null} = no limit / Caffeine default).
     *
     * <h3>Example</h3>
     * <pre>{@code
     * var config = Configuration.builder()
     *     .debounce(Duration.ofMillis(300))       // required: reset window
     *     .maxDebounce(Duration.ofSeconds(3))     // optional: cap total wait
     *     .idle(Duration.ofMinutes(5))            // optional: evict idle keys
     *     .maxSize(10_000L)                      // optional: LRU cap
     *     .tolerance(Duration.ofMillis(5))        // optional: skip micro-renew
     *     .build();
     * }</pre>
     *
     * <h3>Choosing idle and maxDebounce</h3>
     * <p>{@code idle} should be at least {@code maxDebounce × 2} (if set).
     * Otherwise, idle eviction may fire callbacks before {@code maxDebounce}
     * expires under normal conditions.
     */
    @lombok.Getter
    @Accessors(fluent = true)
    @lombok.Builder(
            toBuilder = true,
            builderClassName = "Builder"
    )
    public static class Configuration {
        /**
         * Base debounce time.  On each {@link #call(Object, Runnable)}, the
         * delay window resets to this value (bounded by {@code maxDebounce}).
         *
         * <p>Must be positive.  Typical values: 100–500 ms for UI-style
         * coalescing, 1–5 s for config reload.
         */
        @lombok.NonNull
        final Duration debounce;

        /**
         * Max total wait time for a key.  When {@code null} (default), no upper bound.
         *
         * <p>Ensures a callback fires no later than {@code maxDebounce} after the
         * <b>first</b> {@code call()} for that key, even if {@code call()} is invoked
         * repeatedly (which resets the window).  Without this, a continuously refreshed
         * key never fires.
         *
         * <p>Must be {@code >= debounce} if set.
         */
        @Nullable
        final Duration maxDebounce;

        /**
         * Tolerance for pipeline time drift. When {@code renewTo - now < tolerance},
         * skip renew and let the task fire (default: 0, no skip).
         *
         * <p><b>Purpose</b>: avoid unnecessary re-enqueue when the task is about to fire,
         * reducing DelayQueue overhead under high-frequency calls.
         *
         * <p><b>Side effect</b>: the actual firing time may be up to {@code tolerance}
         * earlier than the ideal renew time. Set to {@code Duration.ZERO} for strict debounce.
         */
        @lombok.NonNull
        @lombok.Builder.Default
        final Duration tolerance = Duration.ZERO;

        /**
         * Idle eviction time.  Sets Caffeine {@code expireAfterAccess}
         * (timer resets on every {@code call()}).
         *
         * <p>When {@code null} (default), no idle eviction.  Entries live
         * until they are explicitly invalidated or evicted by {@code maxSize}.
         *
         * <p><b>Tip:</b> set to at least {@code maxDebounce × 2} (if set)
         * to avoid premature firing.
         */
        @Nullable
        final Duration idle;

        /**
         * Max number of entries the cache will hold.  When {@code null} (default),
         * no size limit.
         *
         * <p>Entries evicted by size pressure fire their callbacks immediately.
         * Set this to a value slightly larger than your expected steady-state
         * key count to avoid premature eviction.
         */
        @Nullable
        final Long maxSize;

        /**
         * Initial hash table size for the cache.  When {@code null} (default),
         * Caffeine uses its default.
         *
         * <p>Set this to avoid rehashing if you know the expected key count
         * upfront.  Must be positive if set.
         */
        @Nullable
        final Integer initCap;

        /**
         * Factory for the internal consumer thread that waits on the
         * {@code DelayQueue} and dispatches callbacks.
         *
         * <p>Default: daemon thread named {@code "debouncer-consumer"}.
         * Override to set priority, name format, or make it non-daemon.
         */
        @lombok.NonNull
        @lombok.Builder.Default
        final ThreadFactory threadFactory = CaffeineDebouncer::defaultThreadFactory;

        /**
         * Create a minimal configuration with only {@code debounce} and
         * {@code maxDebounce}.  Other fields use defaults (all optional).
         *
         * <p>Use this when you don't need idle eviction, size limits, or
         * custom thread factory.  For full control, use
         * {@code Configuration.builder()...build()}.
         *
         * @param debounce    base debounce time (required)
         * @param maxDebounce max total wait time; {@code null} for no limit
         * @return configuration instance
         */
        public static Configuration of(Duration debounce, @Nullable Duration maxDebounce) {
            return Configuration.builder()
                    .debounce(debounce)
                    .maxDebounce(maxDebounce)
                    .build();
        }

        /**
         * Validate this configuration.
         *
         * @throws IllegalArgumentException if any field is invalid
         *                                  (e.g., {@code debounce <= 0}, {@code maxDebounce < debounce})
         */
        void validate() {
            if (debounce.isNegative() || debounce.isZero()) {
                throw new IllegalArgumentException("debounce must be positive");
            }
            if (maxDebounce != null) {
                if (maxDebounce.isNegative() || maxDebounce.isZero()) {
                    throw new IllegalArgumentException("maxDebounce must be positive");
                }
                if (maxDebounce.compareTo(debounce) < 0) {
                    throw new IllegalArgumentException("maxDebounce must be >= debounce");
                }
            }
            if (idle != null && (idle.isNegative() || idle.isZero())) {
                throw new IllegalArgumentException("idle must be positive");
            }
            if (maxSize != null && maxSize <= 0) {
                throw new IllegalArgumentException("maxSize must be positive");
            }
            if (tolerance.isNegative()) {
                throw new IllegalArgumentException("tolerance must be non-negative");
            }
        }
    }
}
