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
package org.febit.common.etcd.locks;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.time.Duration;

@Getter
@lombok.Builder(
        builderClassName = "Builder"
)
@Accessors(fluent = true)
public final class EtcdLockOptions {

    private static final EtcdLockOptions DEFAULT = EtcdLockOptions.builder().build();

    @lombok.Builder.Default
    private final boolean strict = false;
    @lombok.Builder.Default
    private final Duration ttl = Duration.ofSeconds(5);
    @lombok.Builder.Default
    private final Duration tryLockTimeout = Duration.ofSeconds(2);
    @lombok.Builder.Default
    private final Duration waitMax = Duration.ofSeconds(-1);

    public static EtcdLockOptions defaults() {
        return DEFAULT;
    }
}
