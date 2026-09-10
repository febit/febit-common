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
import org.febit.common.rabbit.delay.DelayPolicy;

import java.time.Duration;

/**
 * Rounds the remaining time to whole seconds, so messages share TTL tiers.
 */
public record FixedRoundingDelayPolicy(long roundingNanos) implements DelayPolicy {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    public FixedRoundingDelayPolicy {
        if (roundingNanos < 0 || roundingNanos > NANOS_PER_SECOND) {
            throw new IllegalArgumentException("roundingNanos must be in [0, " + NANOS_PER_SECOND + "]");
        }
    }

    /**
     * @param rounding fraction of a second tolerated when rounding up, in {@code [0, 1]}
     */
    public static FixedRoundingDelayPolicy ofRounding(double rounding) {
        if (rounding < 0 || rounding > 1) {
            throw new IllegalArgumentException("rounding must be in [0, 1]");
        }
        return new FixedRoundingDelayPolicy((long) (rounding * NANOS_PER_SECOND));
    }

    @Override
    public Duration delay(DelayContext ctx) {
        var remaining = Duration.between(ctx.options().clock().instant(), ctx.control().deadline());
        if (!remaining.isPositive()) {
            return Duration.ZERO;
        }
        long secs = remaining.getSeconds();
        int nano = remaining.getNano();
        if (nano == 0) {
            return remaining;
        }
        // nanos needed to round up; always < 1s while nano > 0
        long addNanos = NANOS_PER_SECOND - nano;
        return Duration.ofSeconds(addNanos <= roundingNanos
                ? secs + 1 : secs);
    }
}
