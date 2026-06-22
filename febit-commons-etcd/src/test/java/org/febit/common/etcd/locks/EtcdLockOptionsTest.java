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

import static org.febit.common.etcd.support.TestSupport.DU_10S;
import static org.febit.common.etcd.support.TestSupport.DU_2S;
import static org.febit.common.etcd.support.TestSupport.DU_30S;
import static org.febit.common.etcd.support.TestSupport.DU_5M;
import static org.febit.common.etcd.support.TestSupport.DU_5S;
import static org.junit.jupiter.api.Assertions.*;

class EtcdLockOptionsTest {

    @Test
    void defaults() {
        var opts = EtcdLockOptions.defaults();
        assertFalse(opts.strict());
        assertEquals(DU_5S, opts.ttl());
        assertEquals(DU_2S, opts.tryLockTimeout());
        assertEquals(Duration.ofSeconds(-1), opts.waitMax());
    }

    @Test
    void builder() {
        var opts = EtcdLockOptions.builder()
                .strict(true)
                .ttl(DU_30S)
                .tryLockTimeout(DU_10S)
                .waitMax(DU_5M)
                .build();
        assertTrue(opts.strict());
        assertEquals(DU_30S, opts.ttl());
        assertEquals(DU_10S, opts.tryLockTimeout());
        assertEquals(DU_5M, opts.waitMax());
    }

    @Test
    void builderPartial() {
        var opts = EtcdLockOptions.builder()
                .strict(true)
                .build();
        assertTrue(opts.strict());
        assertEquals(DU_5S, opts.ttl());
        assertEquals(DU_2S, opts.tryLockTimeout());
    }
}
