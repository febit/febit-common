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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import org.febit.common.rabbit.delay.DelayContext;
import org.febit.common.rabbit.delay.DelayQueueMetrics;
import org.febit.common.rabbit.delay.DelayQueueOptions;
import org.febit.common.rabbit.delay.Headers;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Covers what the single-step tests cannot: whether the tier ladder converges, and when the
 * message finally lands.  The broker double walks the hops; this class only states intent.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DelayFlowTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    /**
     * Hops for a 5000s deadline consumed exactly on time: the ladder is
     * {@code 3600 → 900 → 300 → 120 → 60 → 15 → 5} (there is no 1200 tier).
     */
    private static final int ON_TIME_HOPS = 7;

    private final MockBroker broker = new MockBroker("test", NOW);

    @Mock
    private DelayQueueMetrics metrics;

    private DelayQueueOptions options;
    private Dispatcher dispatcher;
    private RehopListener rehopListener;

    @BeforeEach
    void setUp() {
        options = DelayQueueOptions.builder()
                .name("test")
                .connectionFactory(broker.connectionFactory())
                .clock(broker.clock())
                .metrics(metrics)
                .build();
        dispatcher = new Dispatcher(options);
        rehopListener = new RehopListener(options);
    }

    @Test
    void alreadyDueGoesStraightToReady() throws Exception {
        dispatcher.dispatch(context(NOW.minusSeconds(1), 0));

        assertThat(broker.publishes()).hasSize(1);
        assertThat(broker.last().isReady()).isTrue();
        assertThat(broker.clock().instant()).isEqualTo(NOW);
    }

    @Test
    void hopsThroughTiersAndLandsExactlyOnDeadline() throws Exception {
        var deadline = NOW.plusSeconds(5000);
        broker.consumeWith(rehopListener);

        assertThat(dispatcher.dispatch(context(deadline, 0))).isTrue();

        int hops = broker.runUntil(deadline);

        assertThat(broker.readyPublishes()).hasSize(1);
        assertThat(broker.waitPublishes()).hasSize(hops);

        assertThat(broker.clock().instant()).isEqualTo(deadline);
        assertThat(hops).isBetween(1, 20);
        assertHopCounterMatches(hops);
        // observability: each hop reports onRehop, the landing reports onPublishReady
        verify(metrics, times(hops)).onRehop(anyString(), anyInt(), any(Duration.class));
        verify(metrics).onPublishReady(anyString(), anyString());
    }

    @Test
    void lateConsumptionTakesFewerHopsButStillLandsOnDeadline() throws Exception {
        var deadline = NOW.plusSeconds(5000);
        broker.consumeWith(rehopListener);
        assertThat(dispatcher.dispatch(context(deadline, 0))).isTrue();

        // the first park only expires long after its TTL — a stalled consumer
        broker.stall(Duration.ofSeconds(4990));
        int hops = broker.runUntil(deadline);

        // each hop re-computes from the deadline, so waking up late just skips tiers
        assertThat(broker.readyPublishes()).hasSize(1);
        assertThat(broker.waitPublishes()).hasSize(hops);
        assertThat(broker.clock().instant()).isEqualTo(deadline);
        assertThat(hops).isLessThan(ON_TIME_HOPS);
        assertHopCounterMatches(hops);
    }

    @Test
    void stalledPastDeadlineGoesStraightToReady() throws Exception {
        var deadline = NOW.plusSeconds(5000);
        broker.consumeWith(rehopListener);
        assertThat(dispatcher.dispatch(context(deadline, 0))).isTrue();

        broker.stall(Duration.ofSeconds(6000));
        int hops = broker.runUntil(deadline);

        // woken up after the deadline: due immediately, no further parking
        assertThat(broker.readyPublishes()).hasSize(1);
        assertThat(broker.waitPublishes()).hasSize(1);
        assertThat(hops).isEqualTo(1);
        assertHopCounterMatches(hops);
    }

    @Test
    void clockRollbackStillConvergesOnDeadline() throws Exception {
        var deadline = NOW.plusSeconds(5000);
        broker.consumeWith(rehopListener);
        assertThat(dispatcher.dispatch(context(deadline, 0))).isTrue();

        broker.rewind(Duration.ofSeconds(1000));
        int hops = broker.runUntil(deadline);

        // park availability is absolute, so a rollback cannot add hops or overshoot
        assertThat(broker.readyPublishes()).hasSize(1);
        assertThat(broker.clock().instant()).isEqualTo(deadline);
        assertThat(hops).isEqualTo(ON_TIME_HOPS);
        assertHopCounterMatches(hops);
    }

    /**
     * The hop counter the system carries in its own headers must agree with the number of
     * hops actually taken — counting loop iterations only proves what the harness did.
     */
    private void assertHopCounterMatches(int hops) {
        assertThat(broker.waitPublishes())
                .extracting(MockBroker.Publish::attempts)
                .isEqualTo(IntStream.range(0, hops).boxed().toList());
        // the ready publish drops every control header by design
        assertThat(broker.last().isReady()).isTrue();
        assertThat(broker.last().header(Headers.ATTEMPTS)).isNull();
    }

    private DelayContext context(Instant deadline, int attempts) {
        return DelayContexts.of(options, deadline, attempts);
    }
}
