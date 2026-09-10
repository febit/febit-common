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

import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

/**
 * AMQP exchanges and queues implementing delayed delivery.
 *
 * <p>Flow: {@code wait} (park with per-message TTL) → {@code rehop} (collect TTL expiries) →
 * back to {@code wait} until due → {@code ready} (deliver to consumers).  Undeliverable
 * messages end in {@code dlx}, which has no consumers.
 *
 * <p>All names derive from a {@code namespace}.
 */
@lombok.Getter
@Accessors(fluent = true)
public final class DelayQueueTopology {

    private final Rehop rehop;
    private final Ready ready;
    private final Dlx dlx;
    private final Wait waitQueue;

    /**
     * @param readyExchangeOverride overrides {@code <namespace>.ready.ex}
     * @param readyRoutingKey       used when a message specifies no routing key
     * @throws IllegalArgumentException if {@code namespace} is blank
     */
    public DelayQueueTopology(
            @lombok.NonNull String namespace,
            @Nullable String readyExchangeOverride,
            @Nullable String readyRoutingKey
    ) {
        if (namespace.isBlank()) {
            throw new IllegalArgumentException("namespace must not be blank");
        }
        this.waitQueue = new Wait(namespace + ".wait.q");

        var readyExchange = readyExchangeOverride != null
                ? readyExchangeOverride
                : namespace + ".ready.ex";
        this.ready = new Ready(readyExchange, readyRoutingKey);

        var rehopBase = namespace + ".rehop";
        this.rehop = new Rehop(rehopBase + ".ex", rehopBase + ".q");
        this.dlx = new Dlx(rehopBase + ".dlx", rehopBase + ".dlq");
    }

    /**
     * Fanout exchange/queue collecting {@code wait} TTL expiries.
     */
    public record Rehop(String exchange, String queue) {
    }

    /**
     * Topic exchange for final delivery; {@code routingKey} is the fallback.
     */
    public record Ready(String exchange, @Nullable String routingKey) {
    }

    /**
     * Parking lot for undeliverable messages; no consumers attached.
     */
    public record Dlx(String exchange, String queue) {
    }

    /**
     * Quorum queue where messages park with per-message TTL and priority.
     */
    public record Wait(String queue) {
    }
}
