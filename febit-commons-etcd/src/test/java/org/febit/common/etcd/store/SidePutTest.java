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
package org.febit.common.etcd.store;

import io.etcd.jetcd.KV;
import io.etcd.jetcd.Watch;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.febit.common.etcd.store.codec.KVCodec;
import org.febit.common.etcd.support.TestCodecs;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SidePutTest {

    private static KVCodec<String, String> codec() {
        return TestCodecs.stringKVCodec();
    }

    private static EtcdAccessor<String, String> accessor() {
        return EtcdAccessor.<String, String>builder()
                .kv(mock(KV.class))
                .watch(mock(Watch.class))
                .codec(codec())
                .metrics(new EtcdMetrics(new SimpleMeterRegistry()))
                .build();
    }

    @Test
    void ofIsOverwrite() {
        var s = SidePut.of(codec(), "k", "v");
        assertInstanceOf(SidePut.Overwrite.class, s);
        assertEquals("k", s.key());
        assertEquals("v", s.value());
    }

    @Test
    void ofIfAbsentIsGuardedByAbsence() {
        var s = SidePut.ofIfAbsent(codec(), "k", "v");
        assertInstanceOf(SidePut.IfAbsent.class, s);
    }

    @Test
    void ofCasCarriesExpectedRevision() {
        var s = SidePut.ofCas(codec(), "k", "v", 5L);
        assertInstanceOf(SidePut.Cas.class, s);
        assertEquals(5L, ((SidePut.Cas<?, ?>) s).modRevision());
    }

    @Test
    void ofFromAccessorUsesItsCodec() {
        var accessor = accessor();
        var s = SidePut.of(accessor, "k", "v");
        assertEquals("k", s.key());
    }

    @Test
    void ofIfAbsentFromAccessorUsesItsCodec() {
        var accessor = accessor();
        var s = SidePut.ofIfAbsent(accessor, "k", "v");
        assertInstanceOf(SidePut.IfAbsent.class, s);
        assertEquals("k", s.key());
    }

    @Test
    void ofCasFromAccessorCarriesRevision() {
        var accessor = accessor();
        var s = SidePut.ofCas(accessor, "k", "v", 5L);
        assertInstanceOf(SidePut.Cas.class, s);
        assertEquals("k", s.key());
        assertEquals(5L, ((SidePut.Cas<?, ?>) s).modRevision());
    }

    @Test
    void encodedKeyAndValueRoundTrip() {
        var s = SidePut.of(codec(), "k", "v");
        assertEquals("k", new String(s.encodedKey().getBytes(), StandardCharsets.UTF_8));
        assertEquals("v", new String(s.encodedValue().getBytes(), StandardCharsets.UTF_8));
    }
}
