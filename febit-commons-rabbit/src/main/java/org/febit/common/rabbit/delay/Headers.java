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

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Header names reserved by the delay queue.
 *
 * <p>Control data travels with the message so every hop can recompute the remaining time.
 * User headers are namespaced under {@value #USER_PREFIX} and restored before delivery; they
 * must not use the reserved {@value #PREFIX} prefix, and their values must be
 * AMQP-encodable (see {@link #validateUserHeaders(Map)}).
 */
@UtilityClass
public class Headers {

    public static final String PREFIX = "x-febit-delay-";

    public static final String USER_PREFIX = PREFIX + "user-";

    public static final String ID = PREFIX + "id";

    public static final String DEADLINE = PREFIX + "deadline";

    public static final String TOTAL = PREFIX + "total";

    public static final String ATTEMPTS = PREFIX + "attempts";

    public static final String ROUTING_KEY = PREFIX + "rk";

    /**
     * @throws IllegalArgumentException if a key uses the reserved prefix, or a value is not
     *                                  AMQP-encodable (String, boxed primitives, byte[],
     *                                  BigDecimal, or nested List/Map of those)
     */
    public static void validateUserHeaders(Map<String, @Nullable Object> headers) {
        for (var entry : headers.entrySet()) {
            var key = entry.getKey();
            if (key.startsWith(PREFIX)) {
                throw new IllegalArgumentException(
                        "user header must not use reserved prefix '" + PREFIX + "': " + key);
            }
            validateValue(entry.getValue());
        }
    }

    private static void validateValue(@Nullable Object value) {
        if (value == null
                || value instanceof String
                || value instanceof Boolean
                || value instanceof byte[]
                || value instanceof Long
                || value instanceof Integer
                || value instanceof Short
                || value instanceof Byte
                || value instanceof Double
                || value instanceof Float
                || value instanceof BigDecimal) {
            return;
        }
        if (value instanceof List<?> list) {
            for (var item : list) {
                validateValue(item);
            }
            return;
        }
        if (value instanceof Map<?, ?> map) {
            for (var mapEntry : map.entrySet()) {
                if (!(mapEntry.getKey() instanceof String)) {
                    throw new IllegalArgumentException(
                            "header map key must be String, but got: " + mapEntry.getKey());
                }
                validateValue(mapEntry.getValue());
            }
            return;
        }
        throw new IllegalArgumentException(
                "unsupported header value type (not AMQP-encodable): " + value.getClass().getName());
    }
}
