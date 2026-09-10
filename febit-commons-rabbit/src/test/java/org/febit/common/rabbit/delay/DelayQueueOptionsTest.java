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

import org.febit.common.rabbit.delay.internal.FixedRoundingDelayPolicy;
import org.febit.common.rabbit.delay.internal.UuidIdGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DelayQueueOptionsTest {

    @Mock
    private ConnectionFactory connectionFactory;

    @Nested
    class Defaults {

        @Test
        void fillsSensibleDefaults() {
            var options = DelayQueueOptions.builder()
                    .name("ns")
                    .connectionFactory(connectionFactory)
                    .build();

            assertThat(options.name()).isEqualTo("ns");
            assertThat(options.confirmTimeout()).isEqualTo(Duration.ofSeconds(10));
            assertThat(options.metrics()).isSameAs(DelayQueueMetrics.DISCARD);
            assertThat(options.clock()).isNotNull();
            assertThat(options.idGenerator()).isInstanceOf(UuidIdGenerator.class);
            assertThat(options.consumer()).isEqualTo(DelayQueueOptions.Consumer.DEFAULT);
            assertThat(options.policy()).isInstanceOf(FixedRoundingDelayPolicy.class);
            assertThat(options.topology().waitQueue().queue()).isEqualTo("ns.wait.q");
        }
    }

    @Nested
    class Custom {

        @Test
        void honorsProvidedValues() {
            var metrics = new DelayQueueMetrics() {
            };
            var policy = new FixedRoundingDelayPolicy(0);
            var clock = Clock.systemUTC();

            var options = DelayQueueOptions.builder()
                    .name("ns")
                    .connectionFactory(connectionFactory)
                    .clock(clock)
                    .metrics(metrics)
                    .policy(policy)
                    .confirmTimeout(Duration.ofSeconds(5))
                    .readyExchange("ex")
                    .defaultRoutingKey("rk")
                    .build();

            assertThat(options.clock()).isSameAs(clock);
            assertThat(options.metrics()).isSameAs(metrics);
            assertThat(options.policy()).isSameAs(policy);
            assertThat(options.confirmTimeout()).isEqualTo(Duration.ofSeconds(5));
            assertThat(options.topology().ready().exchange()).isEqualTo("ex");
            assertThat(options.topology().ready().routingKey()).isEqualTo("rk");
        }
    }

    @Nested
    class Validation {

        @Test
        void rejectsBlankName() {
            assertThatThrownBy(() -> DelayQueueOptions.builder()
                    .name(" ")
                    .connectionFactory(connectionFactory)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");
        }

        @Test
        void rejectsNonPositiveConfirmTimeout() {
            assertThatThrownBy(() -> DelayQueueOptions.builder()
                    .name("ns")
                    .connectionFactory(connectionFactory)
                    .confirmTimeout(Duration.ZERO)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("confirmTimeout");
        }
    }

    @Nested
    class ConsumerValidation {

        @Test
        void rejectsNonPositiveConcurrency() {
            assertThatThrownBy(() -> new DelayQueueOptions.Consumer(0, 1, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("concurrency");
        }

        @Test
        void rejectsNonPositivePrefetch() {
            assertThatThrownBy(() -> new DelayQueueOptions.Consumer(1, 0, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("prefetch");
        }
    }
}
