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

import com.rabbitmq.client.AMQP;
import org.febit.common.rabbit.delay.internal.DelayTier;
import org.jspecify.annotations.Nullable;
import org.springframework.amqp.core.Message;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.febit.lang.util.Defaults.nvl;

/**
 * State for processing a single hop: queue {@code options}, scheduling {@link Control} and
 * payload {@link Envelope}.
 *
 * <p>A fresh context is built for every hop.
 */
public record DelayContext(
        @lombok.NonNull DelayQueueOptions options,
        @lombok.NonNull Control control,
        @lombok.NonNull Envelope envelope
) {
    private static final int DELIVERY_MODE_PERSISTENT = 2;

    public static DelayContext of(DelayQueueOptions options, Control control, Envelope envelope) {
        return new DelayContext(options, control, envelope);
    }

    @Nullable
    private static String asString(@Nullable Object v) {
        return v instanceof String s ? s : null;
    }

    @Nullable
    private static Long asLong(@Nullable Object v) {
        return v instanceof Number n ? n.longValue() : null;
    }

    @Nullable
    private static Integer asInt(@Nullable Object v) {
        return v instanceof Number n ? n.intValue() : null;
    }

    private AMQP.BasicProperties.Builder toBasePropsBuilder() {
        var builder = new AMQP.BasicProperties.Builder()
                .deliveryMode(DELIVERY_MODE_PERSISTENT);
        if (envelope.contentType() != null) {
            builder.contentType(envelope.contentType());
        }
        if (envelope.contentEncoding() != null) {
            builder.contentEncoding(envelope.contentEncoding());
        }
        return builder;
    }

    /**
     * Final delivery: user headers only, all control headers dropped.
     */
    public AMQP.BasicProperties toReadyProps() {
        var builder = toBasePropsBuilder();
        var userHeaders = envelope.headers();
        builder.headers(userHeaders);
        return builder.build();
    }

    /**
     * Intermediate hop: writes control headers, plus TTL/priority when a tier is given.
     */
    public AMQP.BasicProperties toHopProps(@Nullable DelayTier tier) {
        var builder = toBasePropsBuilder();

        var headers = new HashMap<String, @Nullable Object>();
        control.hopHeaders(headers::put);
        envelope.hopHeaders(headers::put);
        builder.headers(headers);

        if (tier != null) {
            builder.expiration(Long.toString(tier.ttlMillis()));
            builder.priority(tier.priority());
        }
        return builder.build();
    }

    /**
     * Scheduling state carried in AMQP headers across hops.
     *
     * <p>{@code attempts} starts at 1.
     */
    @lombok.Builder(
            builderClassName = "Builder"
    )
    public record Control(
            @lombok.NonNull String id,
            int attempts,
            @lombok.NonNull Instant deadline,
            @lombok.NonNull Duration total,
            @lombok.NonNull String routingKey
    ) {

        /**
         * Reads control state from message headers, incrementing {@code attempts}.
         *
         * @return {@code null} if a required header is missing
         */
        @Nullable
        public static Control from(Message msg) {
            var headers = msg.getMessageProperties().getHeaders();
            var id = asString(headers.get(Headers.ID));
            var deadlineMs = asLong(headers.get(Headers.DEADLINE));
            var totalMs = asLong(headers.get(Headers.TOTAL));
            var attempts = asInt(headers.get(Headers.ATTEMPTS));
            var routingKey = asString(headers.get(Headers.ROUTING_KEY));

            if (id == null || deadlineMs == null || totalMs == null || attempts == null || routingKey == null) {
                return null;
            }
            return builder()
                    .id(id)
                    .attempts(attempts + 1)
                    .deadline(Instant.ofEpochMilli(deadlineMs))
                    .total(Duration.ofMillis(totalMs))
                    .routingKey(routingKey)
                    .build();
        }

        void hopHeaders(BiConsumer<String, @Nullable Object> collector) {
            collector.accept(Headers.ID, id);
            collector.accept(Headers.DEADLINE, deadline.toEpochMilli());
            collector.accept(Headers.TOTAL, total.toMillis());
            collector.accept(Headers.ATTEMPTS, attempts);
            collector.accept(Headers.ROUTING_KEY, routingKey);
        }
    }

    /**
     * Message payload. {@code body} is never {@code null}; empty when absent.
     */
    @lombok.Builder(
            builderClassName = "Builder"
    )
    public record Envelope(
            byte @lombok.NonNull [] body,
            @lombok.NonNull Map<String, @Nullable Object> headers,
            @Nullable String contentType,
            @Nullable String contentEncoding
    ) {

        /**
         * Extracts the payload, stripping the {@code x-febit-delay-user-} header prefix.
         */
        public static Envelope from(Message msg) {
            var props = msg.getMessageProperties();
            var headers = props.getHeaders();
            var contentType = props.getContentType();
            var contentEncoding = props.getContentEncoding();

            var body = nvl(msg.getBody(), new byte[0]);
            var userHeaders = new HashMap<String, @Nullable Object>();

            var prefixLen = Headers.USER_PREFIX.length();
            for (var entry : headers.entrySet()) {
                var key = entry.getKey();
                if (key.startsWith(Headers.USER_PREFIX)) {
                    userHeaders.put(key.substring(prefixLen), entry.getValue());
                }
            }
            return new Envelope(body, userHeaders, contentType, contentEncoding);
        }

        void hopHeaders(BiConsumer<String, @Nullable Object> collector) {
            for (var entry : headers.entrySet()) {
                collector.accept(Headers.USER_PREFIX + entry.getKey(), entry.getValue());
            }
        }
    }

}
