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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import org.febit.common.rabbit.delay.DelayMessage;
import org.febit.common.rabbit.delay.DelayQueueMetrics;
import org.febit.common.rabbit.delay.DelayQueueOptions;
import org.febit.common.rabbit.delay.DelayQueuePublishException;
import org.febit.common.rabbit.delay.Headers;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({
        "resource",
        "java:S5778" // Only one method invocation is expected when testing runtime exceptions
})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DelayQueueImplTest {

    private static final Instant NOW = Instant.parse("2026-07-20T12:00:00Z");
    private static final Instant FUTURE = Instant.parse("2026-07-20T13:00:00Z");
    private static final Instant PAST = Instant.parse("2026-07-20T11:00:00Z");

    @Mock
    private SimpleMessageListenerContainer container;
    @Mock
    private Dispatcher dispatcher;
    @Mock
    private ConnectionFactory connectionFactory;

    private DelayQueueOptions baseOptions() {
        return DelayQueueOptions.builder()
                .name("ns")
                .connectionFactory(connectionFactory)
                .idGenerator(msg -> "fixed-id")
                .clock(Clock.fixed(NOW, ZoneOffset.UTC))
                .build();
    }

    private DelayQueueImpl newImpl(DelayQueueOptions options) throws Exception {
        Constructor<DelayQueueImpl> ctor = DelayQueueImpl.class.getDeclaredConstructor(
                DelayQueueOptions.class, SimpleMessageListenerContainer.class, Dispatcher.class);
        ctor.setAccessible(true);
        return ctor.newInstance(options, container, dispatcher);
    }

    @Test
    void readyExchangeDelegatesToTopology() throws Exception {
        var impl = newImpl(baseOptions());
        assertThat(impl.readyExchange()).isEqualTo("ns.ready.ex");
    }

    @Test
    void connectionFactoryDelegates() throws Exception {
        var impl = newImpl(baseOptions());
        assertThat(impl.connectionFactory()).isSameAs(connectionFactory);
    }

    @Test
    void closeShutsDownRunningContainer() throws Exception {
        when(container.isRunning()).thenReturn(true);
        var impl = newImpl(baseOptions());
        impl.close();
        verify(container).shutdown();
    }

    @Test
    void closeSkipsShutdownWhenNotRunning() throws Exception {
        when(container.isRunning()).thenReturn(false);
        var impl = newImpl(baseOptions());
        impl.close();
        verify(container, never()).shutdown();
    }

    @Test
    void offerReturnsReceiptWithResolvedFields() throws Exception {
        when(dispatcher.dispatch(any())).thenReturn(true);
        var impl = newImpl(baseOptions());
        var receipt = impl.offer(DelayMessage.builder()
                .routingKey("rk")
                .deadline(FUTURE)
                .content("body")
                .build());
        assertThat(receipt.id()).isEqualTo("fixed-id");
        assertThat(receipt.routingKey()).isEqualTo("rk");
        assertThat(receipt.deadline()).isEqualTo(FUTURE);
        assertThat(receipt.delay()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void offerInvokesMetricsOnOffer() throws Exception {
        var offered = new AtomicReference<String>();
        var metrics = new DelayQueueMetrics() {
            @Override
            public void onOffer(String id, String routingKey) {
                offered.set(id + "|" + routingKey);
            }
        };
        var options = DelayQueueOptions.builder()
                .name("ns")
                .connectionFactory(connectionFactory)
                .idGenerator(msg -> "fixed-id")
                .clock(Clock.fixed(NOW, ZoneOffset.UTC))
                .metrics(metrics)
                .build();
        when(dispatcher.dispatch(any())).thenReturn(true);
        var impl = newImpl(options);
        impl.offer(DelayMessage.builder()
                .routingKey("rk")
                .deadline(FUTURE)
                .content("body")
                .build());
        assertThat(offered.get()).isEqualTo("fixed-id|rk");
    }

    @Test
    void offerFallsBackToDefaultRoutingKey() throws Exception {
        when(dispatcher.dispatch(any())).thenReturn(true);
        var options = DelayQueueOptions.builder()
                .name("ns")
                .connectionFactory(connectionFactory)
                .idGenerator(msg -> "fixed-id")
                .clock(Clock.fixed(NOW, ZoneOffset.UTC))
                .defaultRoutingKey("def-rk")
                .build();
        var impl = newImpl(options);
        var receipt = impl.offer(DelayMessage.builder()
                .deadline(FUTURE)
                .content("body")
                .build());
        assertThat(receipt.routingKey()).isEqualTo("def-rk");
    }

    @Test
    void offerWithoutAnyRoutingKeyFails() throws Exception {
        var impl = newImpl(baseOptions());
        assertThatThrownBy(() -> impl.offer(DelayMessage.builder()
                .deadline(FUTURE)
                .content("body")
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("routingKey");
    }

    @Test
    void offerClampsNegativeDelayToZero() throws Exception {
        when(dispatcher.dispatch(any())).thenReturn(true);
        var impl = newImpl(baseOptions());
        var receipt = impl.offer(DelayMessage.builder()
                .routingKey("rk")
                .deadline(PAST)
                .content("body")
                .build());
        assertThat(receipt.delay()).isEqualTo(Duration.ZERO);
    }

    @Test
    void offerRejectsReservedHeaderPrefix() throws Exception {
        var impl = newImpl(baseOptions());
        assertThatThrownBy(() -> impl.offer(DelayMessage.builder()
                .routingKey("rk")
                .deadline(FUTURE)
                .header(Headers.PREFIX + "x", "v")
                .content("body")
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reserved prefix");
    }

    @Test
    void offerThrowsOnConfirmTimeout() throws Exception {
        when(dispatcher.dispatch(any())).thenReturn(false);
        var impl = newImpl(baseOptions());
        assertThatThrownBy(() -> impl.offer(DelayMessage.builder()
                .routingKey("rk")
                .deadline(FUTURE)
                .content("body")
                .build()))
                .isInstanceOf(DelayQueuePublishException.class)
                .hasMessageContaining("broker confirm timeout");
    }

    @Test
    void offerWrapsIOException() throws Exception {
        var io = new IOException("boom");
        when(dispatcher.dispatch(any())).thenThrow(io);
        var impl = newImpl(baseOptions());
        assertThatThrownBy(() -> impl.offer(DelayMessage.builder()
                .routingKey("rk")
                .deadline(FUTURE)
                .content("body")
                .build()))
                .isInstanceOf(DelayQueuePublishException.class)
                .hasMessageContaining("failed to publish")
                .hasCause(io);
    }

    @Test
    void offerWrapsTimeoutException() throws Exception {
        var to = new TimeoutException("wait");
        when(dispatcher.dispatch(any())).thenThrow(to);
        var impl = newImpl(baseOptions());
        assertThatThrownBy(() -> impl.offer(DelayMessage.builder()
                .routingKey("rk")
                .deadline(FUTURE)
                .content("body")
                .build()))
                .isInstanceOf(DelayQueuePublishException.class)
                .hasMessageContaining("timed out")
                .hasCause(to);
    }

    @Test
    void offerRestoresInterruptFlagAndWrapsInterruptedException() throws Exception {
        when(dispatcher.dispatch(any())).thenThrow(new InterruptedException("int"));
        var impl = newImpl(baseOptions());
        try {
            assertThatThrownBy(() -> impl.offer(DelayMessage.builder()
                    .routingKey("rk")
                    .deadline(FUTURE)
                    .content("body")
                    .build()))
                    .isInstanceOf(DelayQueuePublishException.class)
                    .hasMessageContaining("interrupted");
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }
}
