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

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import org.febit.common.rabbit.delay.DelayContext;
import org.febit.common.rabbit.delay.DelayQueueOptions;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class FixedRoundingDelayPolicyTest {

    /**
     * Built from real values: {@code Control}/{@code DelayContext}/{@code DelayQueueOptions} are final types that mock unreliably, and the policy only reads the clock and deadline.
     */
    private static DelayContext context(Instant now, Instant deadline) {
        var options = DelayQueueOptions.builder()
                .name("test")
                .connectionFactory(mock(ConnectionFactory.class))
                .clock(Clock.fixed(now, ZoneOffset.UTC))
                .build();
        return DelayContexts.of(options, deadline, 0);
    }

    @Test
    void returnsZeroWhenExpired() {
        var now = Instant.parse("2026-01-01T00:00:10Z");
        var ctx = context(now, now.minusSeconds(5));
        var policy = new FixedRoundingDelayPolicy(0);
        assertThat(policy.delay(ctx)).isEqualTo(Duration.ZERO);
    }

    @Test
    void returnsZeroWhenDueNow() {
        var now = Instant.parse("2026-01-01T00:00:00Z");
        assertThat(new FixedRoundingDelayPolicy(0).delay(context(now, now)))
                .isEqualTo(Duration.ZERO);
    }

    @Test
    void roundsDownWithinAndUpBeyondHalfSecondTolerance() {
        var now = Instant.parse("2026-01-01T00:00:00Z");
        var policy = FixedRoundingDelayPolicy.ofRounding(0.5);
        // 1.4s remaining -> 400ms to fill, within the 500ms tolerance -> down
        assertThat(policy.delay(context(now, now.plusSeconds(1).plusMillis(400))))
                .isEqualTo(Duration.ofSeconds(1));
        // 1.6s remaining -> 600ms to fill, beyond the 500ms tolerance -> up
        assertThat(policy.delay(context(now, now.plusSeconds(1).plusMillis(600))))
                .isEqualTo(Duration.ofSeconds(2));
    }

    @Test
    void ofRoundingAcceptsSingleNanoInterval() {
        assertThat(FixedRoundingDelayPolicy.ofRounding(0)).isNotNull();
        assertThat(FixedRoundingDelayPolicy.ofRounding(1)).isNotNull();
    }

    @Test
    void returnsWholeSecondsWithoutRounding() {
        var now = Instant.parse("2026-01-01T00:00:00Z");
        var ctx = context(now, now.plusSeconds(7));
        var policy = new FixedRoundingDelayPolicy(0);
        assertThat(policy.delay(ctx)).isEqualTo(Duration.ofSeconds(7));
    }

    @Test
    void roundsUpWhenWithinTolerance() {
        var now = Instant.parse("2026-01-01T00:00:00Z");
        // 5s + 300ms -> addNanos = 700ms tolerance; rounding 0.8s -> ceil
        var ctx = context(now, now.plusSeconds(5).plusMillis(300));
        var policy = FixedRoundingDelayPolicy.ofRounding(0.8);
        assertThat(policy.delay(ctx)).isEqualTo(Duration.ofSeconds(6));
    }

    @Test
    void roundsDownWhenBeyondTolerance() {
        var now = Instant.parse("2026-01-01T00:00:00Z");
        var ctx = context(now, now.plusSeconds(5).plusMillis(300));
        var policy = FixedRoundingDelayPolicy.ofRounding(0.5);
        assertThat(policy.delay(ctx)).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    void alwaysFloorsWhenRoundingNanosIsZero() {
        var now = Instant.parse("2026-01-01T00:00:00Z");
        var ctx = context(now, now.plusSeconds(5).plusNanos(1));
        var policy = new FixedRoundingDelayPolicy(0);
        assertThat(policy.delay(ctx)).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    void alwaysCeilsWhenRoundingNanosIsFullSecond() {
        var now = Instant.parse("2026-01-01T00:00:00Z");
        var ctx = context(now, now.plusSeconds(5).plusNanos(1));
        var policy = FixedRoundingDelayPolicy.ofRounding(1.0);
        assertThat(policy.delay(ctx)).isEqualTo(Duration.ofSeconds(6));
    }

    @Test
    void ofRoundingRejectsOutOfRange() {
        assertThatThrownBy(() -> FixedRoundingDelayPolicy.ofRounding(-0.1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FixedRoundingDelayPolicy.ofRounding(1.1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructorRejectsInvalidRoundingNanos() {
        assertThatThrownBy(() -> new FixedRoundingDelayPolicy(-1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FixedRoundingDelayPolicy(1_000_000_001L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Dual-clock model: {@code delay()} reads "now" from a separate dispatch clock, while the
     * deadline stays anchored to the enqueue epoch.  A dispatch-clock rollback makes the wait only
     * ever grow — longer than intended, but never negative or early (the safe degradation).
     */
    @Test
    void rollbackOfDispatchClockExtendsWaitAndStaysNonNegative() {
        var enqueue = Instant.parse("2026-01-01T00:00:00Z");
        var intendedWait = Duration.ofSeconds(60);
        var deadline = enqueue.plus(intendedWait); // absolute target, anchored to enqueue epoch

        var dispatch = enqueue.minusSeconds(30);   // dispatch clock rolled back 30s
        var policy = new FixedRoundingDelayPolicy(0);

        var actualWait = policy.delay(context(dispatch, deadline));

        // 90s of dispatch-clock time: 30s longer than intended, yet still strictly forward.
        assertThat(actualWait).isEqualTo(Duration.ofSeconds(90));
        assertThat(actualWait).isGreaterThanOrEqualTo(intendedWait);
        assertThat(actualWait).isPositive();
    }

    /**
     * Dual-clock model, the other direction: a dispatch-clock jump past the deadline makes the
     * policy treat the message as already due and return {@link Duration#ZERO} — never negative.
     */
    @Test
    void forwardJumpOfDispatchClockDeliversImmediatelyWithoutNegativeDelay() {
        var enqueue = Instant.parse("2026-01-01T00:00:00Z");
        var deadline = enqueue.plusSeconds(60);

        var dispatch = enqueue.plusSeconds(120);    // dispatch clock jumped 120s ahead
        var policy = new FixedRoundingDelayPolicy(0);

        var actualWait = policy.delay(context(dispatch, deadline));

        assertThat(actualWait).isEqualTo(Duration.ZERO); // due now, never negative
        assertThat(actualWait).isGreaterThanOrEqualTo(Duration.ZERO);
    }
}
