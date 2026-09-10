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

import org.jspecify.annotations.Nullable;

import java.time.Duration;

/**
 * Observation hooks for delayed-message lifecycle events.
 *
 * <p>Every method is a no-op by default.  Implementations must be thread-safe and must not
 * throw — callbacks run on the publish and re-hop paths.
 */
public interface DelayQueueMetrics {

    /**
     * Sink that discards every event.
     */
    DelayQueueMetrics DISCARD = new DelayQueueMetrics() {
    };

    default void onOffer(String id, String routingKey) {
    }

    default void onRehop(String id, int attempts, Duration delay) {
    }

    default void onPublishReady(String id, String routingKey) {
    }

    /**
     * Terminal state: the message is parked in the DLQ and never retried automatically.
     *
     * @param id message id; {@code null} when it cannot be recovered from headers
     */
    default void onDeadLetter(@Nullable String id, int attempts, String reason) {
    }
}
