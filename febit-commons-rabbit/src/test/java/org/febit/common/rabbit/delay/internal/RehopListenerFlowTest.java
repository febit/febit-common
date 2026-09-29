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
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import org.febit.common.rabbit.delay.DelayQueueMetrics;
import org.febit.common.rabbit.delay.DelayQueueOptions;
import org.febit.common.rabbit.delay.Headers;

import java.io.IOException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RehopListenerFlowTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant DEADLINE = Instant.parse("2026-01-01T00:00:10Z");
    private final MockBroker broker = new MockBroker("test", NOW);
    @Mock
    private DelayQueueMetrics metrics;
    private RehopListener listener;

    @BeforeEach
    void setUp() {
        var options = DelayQueueOptions.builder()
                .name("test")
                .connectionFactory(broker.connectionFactory())
                .clock(broker.clock())
                .metrics(metrics)
                .build();
        listener = new RehopListener(options);
    }

    private Message controlMessage(long deliveryTag) {
        var props = new MessageProperties();
        props.setDeliveryTag(deliveryTag);
        props.setHeader(Headers.ID, "id1");
        props.setHeader(Headers.DEADLINE, DEADLINE.toEpochMilli());
        props.setHeader(Headers.TOTAL, 5000L);
        props.setHeader(Headers.ATTEMPTS, 0);
        props.setHeader(Headers.ROUTING_KEY, "rk");
        return new Message(new byte[0], props);
    }

    @Test
    void nacksAndDeadLettersMalformedControl() throws Exception {
        var props = new MessageProperties();
        props.setDeliveryTag(7L);
        var msg = new Message(new byte[0], props);

        listener.onMessage(msg, broker.channel());

        verify(metrics).onDeadLetter(isNull(), eq(-1), eq("parse-failed"));
        verify(broker.channel()).basicNack(7L, false, false);
        verify(broker.channel(), never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void acksWhenDispatchConfirms() throws Exception {
        listener.onMessage(controlMessage(1L), broker.channel());

        verify(broker.channel()).basicAck(1L, false);
        verify(broker.channel(), never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void reentersAndAcksWhenDispatchFailsButReenterConfirms() throws Exception {
        broker.confirmWith(false, true);

        listener.onMessage(controlMessage(1L), broker.channel());

        verify(broker.channel()).basicAck(1L, false);
        verify(broker.channel(), never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void nacksWhenBothDispatchAndReenterFail() throws Exception {
        broker.confirmWith(false, false);

        listener.onMessage(controlMessage(1L), broker.channel());

        verify(broker.channel()).basicNack(1L, false, false);
        verify(broker.channel(), never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void rejectsNullChannel() {
        var msg = controlMessage(1L);

        assertThatThrownBy(() -> listener.onMessage(msg, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("channel");
    }

    @Test
    void nacksWhenReenterPublishFails() throws Exception {
        broker.confirmWith(false);
        broker.failPublishAfter(1, new IOException("boom"));

        listener.onMessage(controlMessage(1L), broker.channel());

        verify(broker.channel()).basicNack(1L, false, false);
        verify(broker.channel(), never()).basicAck(anyLong(), anyBoolean());
    }
}
