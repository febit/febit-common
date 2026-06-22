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
package org.febit.common.etcd.support;

import io.etcd.jetcd.ByteSequence;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@UtilityClass
public class TestSupport {

    public static final Duration DU_20MS = Duration.ofMillis(20);
    public static final Duration DU_50MS = Duration.ofMillis(50);
    public static final Duration DU_200MS = Duration.ofMillis(200);
    public static final Duration DU_300MS = Duration.ofMillis(300);

    public static final Duration DU_1S = Duration.ofSeconds(1);
    public static final Duration DU_2S = Duration.ofSeconds(2);
    public static final Duration DU_5S = Duration.ofSeconds(5);
    public static final Duration DU_10S = Duration.ofSeconds(10);
    public static final Duration DU_15S = Duration.ofSeconds(15);
    public static final Duration DU_30S = Duration.ofSeconds(30);
    public static final Duration DU_60S = Duration.ofSeconds(60);

    public static final Duration DU_5M = Duration.ofMinutes(5);
    public static final Duration DU_10M = Duration.ofMinutes(10);

    public static final ByteSequence LOCK_KEY_DELIMITER = bytes("/");

    public static ByteSequence bytes(String value) {
        return ByteSequence.from(value, StandardCharsets.UTF_8);
    }
}
