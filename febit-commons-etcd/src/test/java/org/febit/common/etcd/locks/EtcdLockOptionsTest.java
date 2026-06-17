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

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class EtcdLockOptionsTest {

    @Test
    void defaultsReturnsSingletonWithSensibleDefaults() {
        var opts = EtcdLockOptions.defaults();
        assertFalse(opts.strict());
        assertEquals(Duration.ofSeconds(5), opts.ttl());
        assertEquals(Duration.ofSeconds(2), opts.tryLockTimeout());
        assertEquals(Duration.ofSeconds(-1), opts.waitMax());
    }

    @Test
    void defaultsReturnsSameInstance() {
        assertSame(EtcdLockOptions.defaults(), EtcdLockOptions.defaults());
    }

    @Test
    void builderProducesCustomOptions() {
        var opts = EtcdLockOptions.builder()
                .strict(true)
                .ttl(Duration.ofSeconds(30))
                .tryLockTimeout(Duration.ofSeconds(10))
                .waitMax(Duration.ofMinutes(5))
                .build();
        assertTrue(opts.strict());
        assertEquals(Duration.ofSeconds(30), opts.ttl());
        assertEquals(Duration.ofSeconds(10), opts.tryLockTimeout());
        assertEquals(Duration.ofMinutes(5), opts.waitMax());
    }

    @Test
    void builderPartialOverridesKeepDefaults() {
        var opts = EtcdLockOptions.builder()
                .strict(true)
                .build();
        assertTrue(opts.strict());
        assertEquals(Duration.ofSeconds(5), opts.ttl());
        assertEquals(Duration.ofSeconds(2), opts.tryLockTimeout());
    }
}
