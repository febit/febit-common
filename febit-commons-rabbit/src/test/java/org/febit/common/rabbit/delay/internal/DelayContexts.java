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
package org.febit.common.rabbit.delay.internal;

import org.febit.common.rabbit.delay.DelayContext;
import org.febit.common.rabbit.delay.DelayQueueOptions;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Builds a {@link DelayContext} with fixed, minimal control/envelope values, so each test can
 * focus on the deadline and the number of attempts it is exercising rather than the boilerplate.
 */
final class DelayContexts {

    private DelayContexts() {
    }

    static DelayContext of(DelayQueueOptions options, Instant deadline, int attempts) {
        var control = DelayContext.Control.builder()
                .id("id")
                .attempts(attempts)
                .deadline(deadline)
                .total(Duration.ZERO)
                .routingKey("rk")
                .build();
        var envelope = DelayContext.Envelope.builder()
                .body(new byte[0])
                .headers(Map.of())
                .build();
        return DelayContext.of(options, control, envelope);
    }
}
