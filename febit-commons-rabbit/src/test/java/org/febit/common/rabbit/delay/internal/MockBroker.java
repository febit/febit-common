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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.febit.common.rabbit.delay.DelayQueueTopology;
import org.febit.common.rabbit.delay.Headers;
import org.jspecify.annotations.Nullable;
import org.mockito.stubbing.Answer;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A broker test double for the delay queue: a published message is parked until its TTL, then
 * re-delivered — so a case advances time instead of waiting.  It answers in domain terms
 * ({@code isReady}/{@code isParked}/{@code isRehop}/{@code ttl}) rather than raw exchange/expiration strings.
 */
final class MockBroker {

    private static final int MAX_HOPS = 100;
    private final DelayQueueTopology topology;
    private final MutableClock clock;
    private final ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
    private final Connection connection = mock(Connection.class);
    private final Channel channel = mock(Channel.class);
    private final List<Publish> publishes = new ArrayList<>();
    private final List<Parked> parked = new ArrayList<>();
    /**
     * Records each publish so a case can assert on it.
     */
    private final Answer<Object> recordingAnswer;
    private long deliveryTag;
    @Nullable
    private RehopListener listener;

    /**
     * @param namespace the same name the options under test are built with, so generated names line up
     * @param start     the instant this broker's clock starts at
     */
    MockBroker(String namespace, Instant start) {
        this.topology = new DelayQueueTopology(namespace, null, null);
        this.clock = new MutableClock(start);
        this.recordingAnswer = inv -> {
            var publish = new Publish(
                    inv.getArgument(0),
                    inv.getArgument(1),
                    inv.getArgument(2),
                    inv.getArgument(3),
                    inv.getArgument(4),
                    topology);
            publishes.add(publish);
            var ttl = publish.ttl();
            if (publish.isParked() && ttl != null) {
                parked.add(new Parked(publish, clock.instant().plus(ttl)));
            }
            return null;
        };
        try {
            when(connectionFactory.createConnection()).thenReturn(connection);
            when(connection.createChannel(false)).thenReturn(channel);
            when(channel.waitForConfirms(anyLong())).thenReturn(true);
            doAnswer(recordingAnswer).when(channel).basicPublish(any(), any(), anyBoolean(), any(), any(byte[].class));
        } catch (Exception e) {
            throw new IllegalStateException("failed to stub the broker double", e);
        }
    }

    private static Instant laterOf(Instant a, Instant b) {
        return a.isAfter(b) ? a : b;
    }

    /**
     * Clock the options are built with; advanced by {@link #runUntil(Instant)}.
     */
    Clock clock() {
        return clock;
    }

    ConnectionFactory connectionFactory() {
        return connectionFactory;
    }

    /**
     * The channel publishes go through, and that a listener acks/nacks on.
     */
    Channel channel() {
        return channel;
    }

    void consumeWith(RehopListener listener) {
        this.listener = listener;
    }

    /**
     * Advances time without consuming, modelling a stalled broker or lagging consumer.
     */
    void stall(Duration d) {
        clock.set(clock.instant().plus(d));
    }

    /**
     * Moves the clock backwards, as an NTP correction would.
     */
    void rewind(Duration d) {
        clock.set(clock.instant().minus(d));
    }

    /**
     * Advances time to {@code target}, re-delivering each parked message once its TTL elapses
     * (walks the tier ladder).  An overdue message is delivered now rather than rewinding the clock.
     *
     * @return the number of hops taken
     */
    int runUntil(Instant target) {
        int hops = 0;
        while (hops <= MAX_HOPS) {
            var next = nextExpiry();
            if (next == null || next.isAfter(target)) {
                break;
            }
            // deliver overdue messages now, never rewind the clock
            clock.set(laterOf(clock.instant(), next));
            deliverExpired();
            hops++;
        }
        if (clock.instant().isBefore(target)) {
            clock.set(target);
        }
        return hops;
    }

    List<Publish> publishes() {
        return publishes;
    }

    Publish last() {
        return publishes.getLast();
    }

    List<Publish> readyPublishes() {
        return publishes.stream().filter(Publish::isReady).toList();
    }

    List<Publish> waitPublishes() {
        return publishes.stream().filter(Publish::isParked).toList();
    }

    /**
     * Forces every subsequent publish to throw (broker/transport failure).
     */
    void failPublishWith(Exception e) throws Exception {
        doThrow(e).when(channel).basicPublish(any(), any(), anyBoolean(), any(), any(byte[].class));
    }

    /**
     * The first {@code succeed} publishes succeed, then every one throws — drives the re-enter failure path.
     */
    void failPublishAfter(int succeed, Exception e) throws Exception {
        var remaining = new AtomicInteger(succeed);
        doAnswer(inv -> {
            if (remaining.getAndDecrement() > 0) {
                recordingAnswer.answer(inv);
                return null;
            }
            // throw once, then revert to normal recording
            doAnswer(recordingAnswer).when(channel).basicPublish(any(), any(), anyBoolean(), any(), any(byte[].class));
            throw e;
        }).when(channel).basicPublish(any(), any(), anyBoolean(), any(), any(byte[].class));
    }

    /**
     * Stubs confirm results for successive publishes; the last value repeats.
     */
    void confirmWith(boolean... results) throws Exception {
        var stubbing = when(channel.waitForConfirms(anyLong()));
        for (var result : results) {
            stubbing = stubbing.thenReturn(result);
        }
    }

    @Nullable
    private Instant nextExpiry() {
        Instant min = null;
        for (var p : parked) {
            if (min == null || p.availableAt().isBefore(min)) {
                min = p.availableAt();
            }
        }
        return min;
    }

    private void deliverExpired() {
        var consumer = this.listener;
        if (consumer == null) {
            return;
        }
        var due = new ArrayList<Parked>();
        parked.removeIf(p -> {
            if (!p.availableAt().isAfter(clock.instant())) {
                due.add(p);
                return true;
            }
            return false;
        });
        for (var p : due) {
            consumer.onMessage(toMessage(p), channel);
        }
    }

    private Message toMessage(Parked p) {
        var props = new MessageProperties();
        var headers = p.publish().props().getHeaders();
        if (headers != null) {
            headers.forEach(props::setHeader);
        }
        props.setDeliveryTag(++deliveryTag);
        return new Message(p.publish().body(), props);
    }

    record Publish(
            String exchange,
            String routingKey,
            boolean mandatory,
            AMQP.BasicProperties props,
            byte[] body,
            DelayQueueTopology topology
    ) {

        boolean isReady() {
            return exchange.equals(topology.ready().exchange());
        }

        boolean isParked() {
            return exchange.isEmpty() && routingKey.equals(topology.waitQueue().queue());
        }

        boolean isRehop() {
            return exchange.equals(topology.rehop().exchange());
        }

        /**
         * Per-message TTL, or {@code null} when none was set.
         */
        @Nullable
        Duration ttl() {
            var expiration = props.getExpiration();
            return expiration == null ? null : Duration.ofMillis(Long.parseLong(expiration));
        }

        @Nullable
        Integer priority() {
            return props.getPriority();
        }

        @Nullable
        Object header(String name) {
            var headers = props.getHeaders();
            return headers == null ? null : headers.get(name);
        }

        /**
         * Hop count from the control headers; {@code -1} when absent.
         */
        int attempts() {
            return header(Headers.ATTEMPTS) instanceof Number n ? n.intValue() : -1;
        }
    }

    private record Parked(Publish publish, Instant availableAt) {
    }

    /**
     * A hand-advanced clock: TTLs elapse by moving it, not by waiting.
     */
    private static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void set(Instant instant) {
            now = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
