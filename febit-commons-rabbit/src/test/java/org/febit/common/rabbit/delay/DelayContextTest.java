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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import org.febit.common.rabbit.delay.internal.DelayTier;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DelayContextTest {

    @Nested
    class ControlFrom {

        private MessageProperties controlMessageProperties(long deadlineMs, int attempts) {
            var props = new MessageProperties();
            props.setHeader(Headers.ID, "id1");
            props.setHeader(Headers.DEADLINE, deadlineMs);
            props.setHeader(Headers.TOTAL, 5000L);
            props.setHeader(Headers.ATTEMPTS, attempts);
            props.setHeader(Headers.ROUTING_KEY, "rk");
            return props;
        }

        @Test
        void parsesAllFieldsAndIncrementsAttempts() {
            var msg = new Message(new byte[0], controlMessageProperties(1000L, 2));

            var control = DelayContext.Control.from(msg);

            assertThat(control).isNotNull();
            assertThat(control.id()).isEqualTo("id1");
            assertThat(control.attempts()).isEqualTo(3);
            assertThat(control.deadline()).isEqualTo(Instant.ofEpochMilli(1000));
            assertThat(control.total()).isEqualTo(Duration.ofMillis(5000));
            assertThat(control.routingKey()).isEqualTo("rk");
        }

        @Test
        void returnsNullWhenAnyRequiredFieldMissing() {
            var props = new MessageProperties();
            props.setHeader(Headers.DEADLINE, 1000L);
            props.setHeader(Headers.TOTAL, 5000L);
            props.setHeader(Headers.ATTEMPTS, 0);
            props.setHeader(Headers.ROUTING_KEY, "rk");
            var msg = new Message(new byte[0], props);

            assertThat(DelayContext.Control.from(msg)).isNull();
        }

        /**
         * Deadline is an absolute enqueue-epoch value that {@code from} must restore byte-for-byte,
         * never re-anchored to {@code clock().instant()} — the 123ms sub-second part also catches truncation.
         */
        @Test
        void deadlineSurvivesHopRoundTripWithoutReanchoring() {
            var deadline = Instant.ofEpochMilli(1_700_000_000_123L); // non-round, carries sub-second
            var control = DelayContext.Control.builder()
                    .id("id1")
                    .attempts(1)
                    .deadline(deadline)
                    .total(Duration.ofMillis(5000))
                    .routingKey("rk")
                    .build();

            // serialize to AMQP headers, as a hop would
            var headers = new HashMap<String, Object>();
            control.hopHeaders(headers::put);

            // deserialize on the next hop
            var nextProps = new MessageProperties();
            headers.forEach(nextProps::setHeader);
            var restored = DelayContext.Control.from(new Message(new byte[0], nextProps));

            assertThat(restored).isNotNull();
            assertThat(restored.deadline()).isEqualTo(deadline); // exact, not re-anchored to clock.now()
            assertThat(restored.attempts()).isEqualTo(2);       // attempts still incremented
        }
    }

    @Nested
    class EnvelopeFrom {

        @Test
        void extractsUserHeadersAndStripsControlPrefix() {
            var props = new MessageProperties();
            props.setHeader(Headers.USER_PREFIX + "foo", "bar");
            props.setHeader(Headers.ID, "id1"); // control header must be filtered out
            props.setContentType("application/json");
            props.setContentEncoding("UTF-8");
            var body = "payload".getBytes(StandardCharsets.UTF_8);
            var msg = new Message(body, props);

            var envelope = DelayContext.Envelope.from(msg);

            assertThat(envelope.body()).isEqualTo(body);
            assertThat(envelope.headers()).containsExactly(Map.entry("foo", "bar"));
            assertThat(envelope.contentType()).isEqualTo("application/json");
            assertThat(envelope.contentEncoding()).isEqualTo("UTF-8");
        }
    }

    @Nested
    class Props {

        private DelayContext contextWithUserHeader(String key, Object value) {
            var options = DelayQueueOptions.builder()
                    .name("test")
                    .connectionFactory(mock(ConnectionFactory.class))
                    .build();
            var control = DelayContext.Control.builder()
                    .id("id1")
                    .attempts(0)
                    .deadline(Instant.now())
                    .total(Duration.ZERO)
                    .routingKey("rk")
                    .build();
            var envelope = DelayContext.Envelope.builder()
                    .body(new byte[0])
                    .headers(Map.of(key, value))
                    .build();
            return DelayContext.of(options, control, envelope);
        }

        @Test
        void readyPropsCarryUserHeadersAndPersistentMode() {
            var props = contextWithUserHeader("foo", "bar").toReadyProps();

            assertThat(props.getHeaders()).containsEntry("foo", "bar");
            assertThat(props.getDeliveryMode()).isEqualTo(2);
            assertThat(props.getExpiration()).isNull();
        }

        @Test
        void hopPropsEmbedControlHeadersAndTier() {
            var ctx = contextWithUserHeader("foo", "bar");
            var props = ctx.toHopProps(new DelayTier(1, 19));

            assertThat(props.getHeaders()).containsKey(Headers.ID);
            assertThat(props.getHeaders()).containsEntry(Headers.USER_PREFIX + "foo", "bar");
            assertThat(props.getExpiration()).isEqualTo("1000");
            assertThat(props.getPriority()).isEqualTo(19);
            assertThat(props.getDeliveryMode()).isEqualTo(2);
        }

        @Test
        void hopPropsWithoutTierOmitExpirationAndPriority() {
            var props = contextWithUserHeader("foo", "bar").toHopProps(null);

            assertThat(props.getExpiration()).isNull();
            assertThat(props.getPriority()).isNull();
        }
    }
}
