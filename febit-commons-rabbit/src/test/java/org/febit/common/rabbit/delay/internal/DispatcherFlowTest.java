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
import org.febit.common.rabbit.delay.DelayQueueMetrics;
import org.febit.common.rabbit.delay.DelayQueueOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DispatcherFlowTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private final MockBroker broker = new MockBroker("test", NOW);
    @Mock
    private DelayQueueMetrics metrics;
    private DelayQueueOptions options;
    private Dispatcher dispatcher;

    @BeforeEach
    void setUp() {
        options = DelayQueueOptions.builder()
                .name("test")
                .connectionFactory(broker.connectionFactory())
                .clock(broker.clock())
                .metrics(metrics)
                .build();
        dispatcher = new Dispatcher(options);
    }

    private DelayContext context(Instant deadline, int attempts) {
        return DelayContexts.of(options, deadline, attempts);
    }

    @Nested
    class Dispatch {

        @Test
        void routesExpiredMessageToReadyExchange() throws Exception {
            assertThat(dispatcher.dispatch(context(NOW.minusSeconds(1), 0))).isTrue();

            var publish = broker.last();
            assertThat(publish.isReady()).isTrue();
            assertThat(publish.mandatory()).isTrue();
            assertThat(publish.ttl()).isNull();
        }

        @Test
        void routesNearTermMessageToWaitQueueWithTierOne() throws Exception {
            dispatcher.dispatch(context(NOW.plusSeconds(1), 0));

            var publish = broker.last();
            assertThat(publish.isParked()).isTrue();
            assertThat(publish.mandatory()).isFalse();
            assertThat(publish.ttl()).isEqualTo(Duration.ofSeconds(1));
            assertThat(publish.priority()).isEqualTo(19);
        }

        @Test
        void selectsLongerTierForMidRangeDelay() throws Exception {
            dispatcher.dispatch(context(NOW.plusSeconds(3), 0));

            assertThat(broker.last().ttl()).isEqualTo(Duration.ofSeconds(2));
            assertThat(broker.last().priority()).isEqualTo(18);
        }

        @Test
        void capsAtLargestTierForVeryLongDelay() throws Exception {
            dispatcher.dispatch(context(NOW.plusSeconds(100_000), 0));

            assertThat(broker.last().ttl()).isEqualTo(Duration.ofHours(12));
        }

        @Test
        void notifiesPublishReadyWhenDue() throws Exception {
            dispatcher.dispatch(context(NOW.minusSeconds(1), 0));

            verify(metrics).onPublishReady("id", "rk");
        }

        @Test
        void notifiesRehopWithTierDuration() throws Exception {
            dispatcher.dispatch(context(NOW.plusSeconds(3), 0));

            verify(metrics).onRehop("id", 0, Duration.ofSeconds(2));
        }

        @Test
        void returnsFalseWhenBrokerDoesNotConfirm() throws Exception {
            broker.confirmWith(false);

            assertThat(dispatcher.dispatch(context(NOW.plusSeconds(1), 0))).isFalse();
        }

        @Test
        void propagatesPublishIOException() throws Exception {
            broker.failPublishWith(new IOException("boom"));

            assertThatThrownBy(() -> dispatcher.dispatch(context(NOW.plusSeconds(1), 0)))
                    .isInstanceOf(IOException.class);
        }
    }

    @Nested
    class Reenter {

        @Test
        void publishesToRehopExchangeWithoutTtl() throws Exception {
            assertThat(dispatcher.reenter(context(NOW.plusSeconds(1), 0))).isTrue();

            var publish = broker.last();
            assertThat(publish.isRehop()).isTrue();
            assertThat(publish.mandatory()).isFalse();
            assertThat(publish.ttl()).isNull();
            assertThat(publish.priority()).isNull();
        }
    }
}
