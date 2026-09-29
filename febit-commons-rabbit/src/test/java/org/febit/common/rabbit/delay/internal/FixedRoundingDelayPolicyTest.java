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

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.tabletest.junit.TableTest;

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

    private static DelayContext context(Instant now, Instant deadline) {
        var options = DelayQueueOptions.builder()
                .name("test")
                .connectionFactory(mock(ConnectionFactory.class))
                .clock(Clock.fixed(now, ZoneOffset.UTC))
                .build();
        return DelayContexts.of(options, deadline, 0);
    }

    @TableTest("""
            nowInstant             | deadlineInstant                  | rounding | expectedSeconds
            "2026-01-01T00:00:10Z" | "2026-01-01T00:00:05Z"           | 0        | 0
            "2026-01-01T00:00:00Z" | "2026-01-01T00:00:00Z"           | 0        | 0
            "2026-01-01T00:00:00Z" | "2026-01-01T00:00:01.4Z"         | 0.5      | 1
            "2026-01-01T00:00:00Z" | "2026-01-01T00:00:01.6Z"         | 0.5      | 2
            "2026-01-01T00:00:00Z" | "2026-01-01T00:00:07Z"           | 0        | 7
            "2026-01-01T00:00:00Z" | "2026-01-01T00:00:05.3Z"         | 0.8      | 6
            "2026-01-01T00:00:00Z" | "2026-01-01T00:00:05.3Z"         | 0.5      | 5
            "2026-01-01T00:00:00Z" | "2026-01-01T00:00:05.000000001Z" | 0        | 5
            "2026-01-01T00:00:00Z" | "2026-01-01T00:00:05.000000001Z" | 1.0      | 6
            "2025-12-31T23:59:30Z" | "2026-01-01T00:01:00Z"           | 0        | 90
            "2026-01-01T00:02:00Z" | "2026-01-01T00:01:00Z"           | 0        | 0
            """)
    void delay(String nowInstant, String deadlineInstant, double rounding, long expectedSeconds) {
        var now = Instant.parse(nowInstant);
        var deadline = Instant.parse(deadlineInstant);
        var policy = FixedRoundingDelayPolicy.ofRounding(rounding);
        assertThat(policy.delay(context(now, deadline)))
                .isEqualTo(Duration.ofSeconds(expectedSeconds));
    }

    @TableTest("""
            rounding | expectThrows
            0        | false
            1        | false
            -0.1     | true
            1.1      | true
            """)
    void ofRoundingRange(double rounding, boolean expectThrows) {
        if (expectThrows) {
            assertThatThrownBy(() -> FixedRoundingDelayPolicy.ofRounding(rounding))
                    .isInstanceOf(IllegalArgumentException.class);
        } else {
            assertThat(FixedRoundingDelayPolicy.ofRounding(rounding)).isNotNull();
        }
    }

    @TableTest("""
            nanos      | expectThrows
            -1         | true
            0          | false
            500        | false
            1000000001 | true
            """)
    void constructorRange(long nanos, boolean expectThrows) {
        if (expectThrows) {
            assertThatThrownBy(() -> new FixedRoundingDelayPolicy(nanos))
                    .isInstanceOf(IllegalArgumentException.class);
        } else {
            assertThat(new FixedRoundingDelayPolicy(nanos)).isNotNull();
        }
    }
}
