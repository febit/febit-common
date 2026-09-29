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
package org.febit.common.rabbit.delay;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import org.febit.common.rabbit.delay.internal.DelayQueueFactory;
import org.febit.common.rabbit.delay.internal.FixedRoundingDelayPolicy;
import org.febit.common.rabbit.delay.internal.UuidIdGenerator;

import lombok.AccessLevel;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

import java.time.Clock;
import java.time.Duration;

import static org.febit.lang.util.Defaults.nvl;

/**
 * Configuration for a {@link RabbitDelayQueue}, and the factory that creates one.
 *
 * <p>Only {@code name} and {@code connectionFactory} are required; everything else has a
 * sensible default.  Names of the underlying exchanges and queues are derived from
 * {@code name}, so several delay queues can share one broker without colliding.
 *
 * <h3>Quick start</h3>
 * <pre>{@code
 * var options = DelayQueueOptions.builder()
 *         .name("orders")                      // required: namespace for all AMQP names
 *         .connectionFactory(connectionFactory) // required
 *         .confirmTimeout(Duration.ofSeconds(10))
 *         .policy(FixedRoundingDelayPolicy.ofRounding(1.0))
 *         .metrics(metrics)
 *         .build();
 *
 * try (var queue = options.createQueue()) {
 *     queue.offer("orders.cancel", "{\"id\":42}", Duration.ofMinutes(30));
 * }
 * }</pre>
 *
 * <p>Instances are immutable and thread-safe.
 *
 * @see RabbitDelayQueue
 */
@lombok.Getter
@Accessors(fluent = true)
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.Builder(
        builderClassName = "InternalBuilder",
        builderMethodName = "internalBuilder",
        access = AccessLevel.PRIVATE
)
public final class DelayQueueOptions {

    /**
     * Namespace for all generated exchange/queue names; must not be blank.
     */
    @lombok.NonNull
    private final String name;

    @lombok.NonNull
    private final ConnectionFactory connectionFactory;

    /**
     * Time source for deadlines; defaults to {@link Clock#systemUTC()}.
     */
    @lombok.NonNull
    private final Clock clock;

    @lombok.NonNull
    private final MessageIdGenerator idGenerator;

    /**
     * Concurrency, prefetch and thread model of the internal re-hop listener.
     */
    @lombok.NonNull
    private final DelayQueueOptions.Consumer consumer;

    /**
     * Publisher-confirm timeout; must be positive. Defaults to 10s.
     */
    @lombok.NonNull
    private final Duration confirmTimeout;

    @lombok.NonNull
    private final DelayQueueMetrics metrics;

    /**
     * Per-hop delay strategy. Defaults to rounding up to the next whole second.
     */
    @lombok.NonNull
    private final DelayPolicy policy;

    @lombok.NonNull
    private final DelayQueueTopology topology;

    @lombok.Builder(
            builderClassName = "Builder"
    )
    private static DelayQueueOptions create(
            @lombok.NonNull String name,
            @lombok.NonNull ConnectionFactory connectionFactory,
            @Nullable Clock clock,
            @Nullable MessageIdGenerator idGenerator,
            @Nullable Consumer consumer,
            @Nullable Duration confirmTimeout,
            @Nullable DelayQueueMetrics metrics,
            @Nullable String defaultRoutingKey,
            @Nullable String readyExchange,
            @Nullable DelayPolicy policy
    ) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        clock = nvl(clock, Clock::systemUTC);
        idGenerator = nvl(idGenerator, UuidIdGenerator.V7);
        consumer = nvl(consumer, Consumer.DEFAULT);
        confirmTimeout = nvl(confirmTimeout, Duration.ofSeconds(10));
        metrics = nvl(metrics, DelayQueueMetrics.DISCARD);

        if (!confirmTimeout.isPositive()) {
            throw new IllegalArgumentException("confirmTimeout must be positive");
        }
        if (policy == null) {
            policy = FixedRoundingDelayPolicy.ofRounding(1.0);
        }

        var topology = new DelayQueueTopology(name, readyExchange, defaultRoutingKey);
        return DelayQueueOptions.internalBuilder()
                .name(name)
                .connectionFactory(connectionFactory)
                .clock(clock)
                .idGenerator(idGenerator)
                .consumer(consumer)
                .confirmTimeout(confirmTimeout)
                .metrics(metrics)
                .policy(policy)
                .topology(topology)
                .build();
    }

    /**
     * Declares the topology on the broker. The returned queue must be closed.
     */
    public RabbitDelayQueue createQueue() {
        return DelayQueueFactory.create(this);
    }

    /**
     * Settings for the internal re-hop listener.
     */
    public record Consumer(
            int concurrency,
            int prefetch,
            boolean virtualThread
    ) {
        /**
         * Default listener settings: one consumer, prefetch 10, platform threads.
         */
        public static final Consumer DEFAULT = new Consumer(1, 10, false);

        public Consumer {
            if (concurrency <= 0) {
                throw new IllegalArgumentException("concurrency must be positive");
            }
            if (prefetch <= 0) {
                throw new IllegalArgumentException("prefetch must be positive");
            }
        }
    }
}
