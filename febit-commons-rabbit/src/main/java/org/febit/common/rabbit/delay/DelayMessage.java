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

import lombok.AccessLevel;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * An immutable message scheduled for delayed delivery.
 *
 * <p>A message carries the payload plus the delivery coordinates: an absolute
 * {@linkplain #deadline() deadline} and an optional {@linkplain #routingKey() routing key}
 * (when absent, the queue's default is used).  Instances are created with a builder.
 *
 *
 * <pre>{@code
 * var msg = DelayMessage.builder()
 *         .routingKey("orders.cancel")
 *         .delay(Duration.ofMinutes(30))
 *         .jsonContent("{\"id\":42}")
 *         .build();
 * }</pre>
 *
 * <p><b>Note:</b> {@code delay(...)} resolves {@code Instant.now()} when called, so it is
 * relative to build time, not to publication.
 */
@lombok.Getter
@Accessors(fluent = true)
@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DelayMessage {

    private final @Nullable String routingKey;
    private final Instant deadline;
    private final Map<String, @Nullable Object> headers;
    private final byte[] content;
    private final @Nullable String contentType;
    private final @Nullable String contentEncoding;

    @lombok.Builder(
            builderClassName = "Builder"
    )
    private static DelayMessage create(
            @lombok.NonNull Instant deadline,
            @lombok.Singular Map<String, @Nullable Object> headers,
            @Nullable String routingKey,
            byte @Nullable [] content,
            @Nullable String contentType,
            @Nullable String contentEncoding
    ) {
        if (content == null) {
            content = new byte[0];
        }
        return new DelayMessage(
                routingKey,
                deadline,
                headers,
                content,
                contentType,
                contentEncoding
        );
    }

    public static class Builder {

        public Builder delay(Duration delay) {
            return deadline(Instant.now().plus(delay));
        }

        public Builder jsonContent(String text) {
            return content(text.getBytes(StandardCharsets.UTF_8))
                    .contentType("application/json")
                    .contentEncoding(StandardCharsets.UTF_8.name());
        }

        @Tolerate
        public Builder content(String text) {
            return content(text.getBytes(StandardCharsets.UTF_8))
                    .contentEncoding(StandardCharsets.UTF_8.name());
        }
    }
}
