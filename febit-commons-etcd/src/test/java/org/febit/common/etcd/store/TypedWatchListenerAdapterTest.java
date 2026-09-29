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

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import org.febit.common.etcd.store.codec.Codec;
import org.febit.common.etcd.store.codec.GenericKVCodec;
import org.febit.common.etcd.support.TestCodecs;
import org.febit.common.etcd.support.TestSupport;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.febit.common.etcd.support.TestSupport.kvOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TypedWatchListenerAdapterTest {

    @SuppressWarnings("unchecked")
    private static TypedWatchObserver<String, String> mockObserver() {
        return mock(TypedWatchObserver.class);
    }

    private static WatchResponse responseWith(WatchEvent.EventType type, String key, String value, long rev) {
        var event = new WatchEvent(kvOf(key, value, rev), null, type);
        var resp = mock(WatchResponse.class);
        when(resp.getEvents()).thenReturn(List.of(event));
        return resp;
    }

    private TypedWatchListenerAdapter<String, String> adapter(TypedWatchObserver<String, String> observer) {
        return TypedWatchListenerAdapter.of(
                TestSupport.bytes("target"),
                TestCodecs.stringKVCodec(),
                observer,
                new EtcdMetrics(new SimpleMeterRegistry())
        );
    }

    @Test
    void onNextDecodesEventAndForwardsToObserver() {
        var observer = mockObserver();
        var listener = adapter(observer);
        listener.onNext(responseWith(WatchEvent.EventType.PUT, "k", "v", 7L));

        verify(observer).onEvent(eq("k"), isNull(), eq("v"), eq(WatchEvent.EventType.PUT), eq(7L));
    }

    @Test
    void onNextSkipsUnparseableKey() {
        var keyCodec = new Codec<String>() {
            @Override
            public ByteSequence encode(String obj) {
                return TestSupport.bytes(obj);
            }

            @Override
            public String decode(ByteSequence bytes) {
                var text = bytes == null ? null : new String(bytes.getBytes(), StandardCharsets.UTF_8);
                return "undecodable-garbage".equals(text) ? null : text;
            }

            @Override
            public String decode(String text) {
                return "undecodable-garbage".equals(text) ? null : text;
            }
        };
        var codec = GenericKVCodec.of(keyCodec, TestCodecs.stringCodec());

        var kv = kvOf("undecodable-garbage", "v", 1L);
        var event = new WatchEvent(kv, null, WatchEvent.EventType.PUT);
        var resp = mock(WatchResponse.class);
        when(resp.getEvents()).thenReturn(List.of(event));

        var observer = mockObserver();
        var listener = TypedWatchListenerAdapter.of(
                TestSupport.bytes("t"),
                codec,
                observer,
                new EtcdMetrics(new SimpleMeterRegistry())
        );
        listener.onNext(resp);

        verifyNoInteractions(observer);
    }

    @Test
    void onNextSkipsEventWhenKeyDecodeThrows() {
        var registry = new SimpleMeterRegistry();
        var observer = mockObserver();
        var keyCodec = new Codec<String>() {
            @Override
            public ByteSequence encode(String obj) {
                return TestSupport.bytes(obj);
            }

            @Override
            public String decode(ByteSequence bytes) {
                throw new IllegalStateException("bad key");
            }

            @Override
            public String decode(String text) {
                return text;
            }
        };
        var codec = GenericKVCodec.of(keyCodec, TestCodecs.stringCodec());
        var event = new WatchEvent(kvOf("k", "v", 1L), null, WatchEvent.EventType.PUT);
        var resp = mock(WatchResponse.class);
        when(resp.getEvents()).thenReturn(List.of(event));

        var listener = TypedWatchListenerAdapter.of(TestSupport.bytes("t"), codec, observer, new EtcdMetrics(registry));
        listener.onNext(resp);

        verifyNoInteractions(observer);
        assertNotNull(registry.find("etcd.error").tag("error_type", "illegal_state").counter());
    }

    @Test
    void onErrorNotifiesObserverAndRequestsReconnect() {
        var registry = new SimpleMeterRegistry();
        var observer = mockObserver();
        var listener = TypedWatchListenerAdapter.of(
                TestSupport.bytes("target"),
                TestCodecs.stringKVCodec(),
                observer,
                new EtcdMetrics(registry)
        );
        listener.onError(new RuntimeException("boom"));

        assertEquals(1.0, registry.find("etcd.watch.reconnect").counter().count(), 0.0);
        verify(observer).onError(any());
        verify(observer).onReconnected();
    }

    @Test
    void onCompletedNotifiesObserverAndCountsCompletion() {
        var registry = new SimpleMeterRegistry();
        var observer = mockObserver();
        var listener = TypedWatchListenerAdapter.of(
                TestSupport.bytes("target"),
                TestCodecs.stringKVCodec(),
                observer,
                new EtcdMetrics(registry)
        );
        listener.onCompleted();

        assertEquals(1.0, registry.find("etcd.watch.completed").counter().count(), 0.0);
        verify(observer).onCompleted();
    }
}
