/*
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
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

import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.febit.common.etcd.support.TestSupport.bytes;
import static org.febit.common.etcd.support.TestSupport.kvOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EtcdPagingScannerTest {

    @Test
    void prefixEndOfIncrementsLastByte() {
        var end = EtcdPagingScanner.prefixEndOf("ab");
        assertEquals("ac", new String(end.getBytes(), StandardCharsets.UTF_8));
    }

    @Test
    void incrementKeyAppendsZeroByte() {
        var next = EtcdPagingScanner.incrementKey(bytes("ab"));
        var bytes = next.getBytes();
        assertEquals(3, bytes.length);
        assertEquals('a', bytes[0]);
        assertEquals('b', bytes[1]);
        assertEquals(0, bytes[2]);
    }

    @Test
    @SuppressWarnings("unchecked")
    void iteratesAcrossMultiplePages() {
        var client = mock(Client.class, RETURNS_DEEP_STUBS);
        var r1 = mock(GetResponse.class);
        when(r1.getKvs()).thenReturn(List.of(kvOf("p/a"), kvOf("p/b")));
        var r2 = mock(GetResponse.class);
        when(r2.getKvs()).thenReturn(List.of(kvOf("p/c")));
        var r3 = mock(GetResponse.class);
        when(r3.getKvs()).thenReturn(List.of());
        when(client.getKVClient().get(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(r1),
                        CompletableFuture.completedFuture(r2),
                        CompletableFuture.completedFuture(r3));

        var scanner = EtcdPagingScanner.ofPrefix(client, "p", 2);
        var collected = new ArrayList<KeyValue>();
        scanner.forEachRemaining(collected::add);

        assertEquals(3, collected.size());
    }

    @Test
    void nextThrowsWhenExhausted() {
        var client = mock(Client.class, RETURNS_DEEP_STUBS);
        var r1 = mock(GetResponse.class);
        when(r1.getKvs()).thenReturn(List.of());
        when(client.getKVClient().get(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(r1));

        var scanner = EtcdPagingScanner.ofPrefix(client, "p", 2);
        assertFalse(scanner.hasNext());
        assertThatThrownBy(scanner::next).isInstanceOf(NoSuchElementException.class);
    }
}
