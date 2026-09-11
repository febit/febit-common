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

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Response.Header;
import io.etcd.jetcd.Txn;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.febit.common.etcd.support.TestCodecs;
import org.febit.common.etcd.support.TestSupport;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.febit.common.etcd.support.TestSupport.kvOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EtcdAccessorTest {

    private static EtcdAccessor<String, String> accessor(KV kv, Watch watch) {
        return EtcdAccessor.<String, String>builder()
                .kv(kv)
                .watch(watch)
                .codec(TestCodecs.stringKVCodec())
                .metrics(new EtcdMetrics(new SimpleMeterRegistry()))
                .build();
    }

    private static Txn txnReturning(TxnResponse resp) {
        var txn = mock(Txn.class);
        when(txn.If(any())).thenReturn(txn);
        when(txn.Then(any())).thenReturn(txn);
        when(txn.Else(any())).thenReturn(txn);
        when(txn.commit()).thenReturn(CompletableFuture.completedFuture(resp));
        return txn;
    }

    @SuppressWarnings("unchecked")
    private static TypedWatchObserver<String, String> mockObserver() {
        return mock(TypedWatchObserver.class);
    }

    private static TxnResponse txnSucceeded(long revision) {
        var resp = mock(TxnResponse.class);
        when(resp.isSucceeded()).thenReturn(true);
        var header = mock(Header.class);
        when(header.getRevision()).thenReturn(revision);
        when(resp.getHeader()).thenReturn(header);
        return resp;
    }

    private static TxnResponse txnFailed() {
        var resp = mock(TxnResponse.class);
        when(resp.isSucceeded()).thenReturn(false);
        return resp;
    }

    @Test
    void getReturnsDecodedRecordWhenPresent() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var resp = mock(GetResponse.class);
        when(resp.getCount()).thenReturn(1L);
        when(resp.getKvs()).thenReturn(List.of(kvOf("k", "v", 42L)));
        when(kv.get(any(ByteSequence.class)))
                .thenReturn(CompletableFuture.completedFuture(resp));

        var accessor = accessor(kv, mock(Watch.class));
        var rec = accessor.get("k");

        assertTrue(rec.isPresent());
        assertEquals("k", rec.get().key());
        assertEquals("v", rec.get().value());
        assertEquals(42L, rec.get().revision());
    }

    @Test
    void getReturnsEmptyWhenCountIsZero() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var resp = mock(GetResponse.class);
        when(resp.getCount()).thenReturn(0L);
        when(kv.get(any(ByteSequence.class)))
                .thenReturn(CompletableFuture.completedFuture(resp));

        var accessor = accessor(kv, mock(Watch.class));
        assertTrue(accessor.get("k").isEmpty());
    }

    @Test
    void getReturnsEmptyWhenValueUnparseable() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var resp = mock(GetResponse.class);
        when(resp.getCount()).thenReturn(1L);
        var keyValue = mock(KeyValue.class);
        when(keyValue.getKey()).thenReturn(TestSupport.bytes("k"));
        when(keyValue.getValue()).thenReturn(null); // value codec yields null
        when(resp.getKvs()).thenReturn(List.of(keyValue));
        when(kv.get(any(ByteSequence.class)))
                .thenReturn(CompletableFuture.completedFuture(resp));

        var accessor = accessor(kv, mock(Watch.class));
        assertTrue(accessor.get("k").isEmpty());
    }

    @Test
    void listWithPrefixDecodesAllMatchingKeys() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var resp = mock(GetResponse.class);
        when(resp.getKvs()).thenReturn(List.of(
                kvOf("p/a", "1", 1L),
                kvOf("p/b", "2", 2L)
        ));
        when(kv.get(any(ByteSequence.class), any(GetOption.class)))
                .thenReturn(CompletableFuture.completedFuture(resp));

        var accessor = accessor(kv, mock(Watch.class));
        var records = accessor.listWithPrefix("p");
        assertEquals(2, records.size());
        assertEquals("p/a", records.get(0).key());
    }

    @Test
    void listKeysDecodesKeysOnly() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var resp = mock(GetResponse.class);
        when(resp.getKvs()).thenReturn(List.of(
                kvOf("p/a", "1", 1L),
                kvOf("p/b", "2", 2L)
        ));
        when(kv.get(any(ByteSequence.class), any(GetOption.class)))
                .thenReturn(CompletableFuture.completedFuture(resp));

        var accessor = accessor(kv, mock(Watch.class));
        var keys = accessor.listKeys("p");
        assertEquals(List.of("p/a", "p/b"), keys);
    }

    @Test
    void listWithPrefixWithRevisionCapturesHeaderRevision() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var resp = mock(GetResponse.class);
        when(resp.getKvs()).thenReturn(List.of(kvOf("p/a", "1", 1L)));
        var header = mock(Header.class);
        when(header.getRevision()).thenReturn(77L);
        when(resp.getHeader()).thenReturn(header);
        when(kv.get(any(ByteSequence.class), any(GetOption.class)))
                .thenReturn(CompletableFuture.completedFuture(resp));

        var accessor = accessor(kv, mock(Watch.class));
        var scan = accessor.listWithPrefixWithRevision("p");
        assertEquals(77L, scan.revision());
        assertEquals(1, scan.records().size());
    }

    @Test
    void countWithPrefixReturnsCount() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var resp = mock(GetResponse.class);
        when(resp.getCount()).thenReturn(3L);
        when(kv.get(any(ByteSequence.class), any(GetOption.class)))
                .thenReturn(CompletableFuture.completedFuture(resp));

        var accessor = accessor(kv, mock(Watch.class));
        assertEquals(3L, accessor.countWithPrefix("p"));
    }

    @Test
    void casReturnsRevisionOnSuccess() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var txn = txnReturning(txnSucceeded(99L));
        when(kv.txn()).thenReturn(txn);

        var accessor = accessor(kv, mock(Watch.class));
        var result = accessor.cas("k", "v", 42L);
        assertTrue(result.isPresent());
        assertEquals(99L, result.getAsLong());
    }

    @Test
    void casReturnsEmptyOnConflict() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var txn = txnReturning(txnFailed());
        when(kv.txn()).thenReturn(txn);

        var accessor = accessor(kv, mock(Watch.class));
        assertTrue(accessor.cas("k", "v", 42L).isEmpty());
    }

    @Test
    void putIfAbsentGuardsWithVersionZero() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var txn = txnReturning(txnSucceeded(5L));
        when(kv.txn()).thenReturn(txn);

        var accessor = accessor(kv, mock(Watch.class));
        var result = accessor.putIfAbsent("k", "v");
        assertTrue(result.isPresent());
        assertEquals(5L, result.getAsLong());
    }

    @Test
    void putReturnsRevision() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var resp = mock(PutResponse.class);
        var header = mock(Header.class);
        when(header.getRevision()).thenReturn(11L);
        when(resp.getHeader()).thenReturn(header);
        when(kv.put(any(), any())).thenReturn(CompletableFuture.completedFuture(resp));

        var accessor = accessor(kv, mock(Watch.class));
        assertEquals(11L, accessor.put("k", "v"));
    }

    @Test
    void putWithLeaseReturnsRevision() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var resp = mock(PutResponse.class);
        var header = mock(Header.class);
        when(header.getRevision()).thenReturn(12L);
        when(resp.getHeader()).thenReturn(header);
        when(kv.put(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(resp));

        var accessor = accessor(kv, mock(Watch.class));
        assertEquals(12L, accessor.putWithLease("k", "v", 1L));
    }

    @Test
    void deleteReturnsDeletedCount() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var resp = mock(DeleteResponse.class);
        when(resp.getDeleted()).thenReturn(1L);
        when(kv.delete(any())).thenReturn(CompletableFuture.completedFuture(resp));

        var accessor = accessor(kv, mock(Watch.class));
        assertEquals(1L, accessor.delete("k"));
    }

    @Test
    void deleteWithPrefixReturnsDeletedCount() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var resp = mock(DeleteResponse.class);
        when(resp.getDeleted()).thenReturn(2L);
        when(kv.delete(any(), any())).thenReturn(CompletableFuture.completedFuture(resp));

        var accessor = accessor(kv, mock(Watch.class));
        assertEquals(2L, accessor.deleteWithPrefix("p"));
    }

    @Test
    void watchReturnsWatcher() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var watch = mock(Watch.class);
        var watcher = mock(Watch.Watcher.class);
        when(watch.watch(
                any(ByteSequence.class),
                any(WatchOption.class),
                any(TypedWatchListenerAdapter.class)
        )).thenReturn(watcher);

        var accessor = accessor(kv, watch);
        assertSame(watcher, accessor.watch("k", mockObserver()));
    }

    @Test
    void watchPrefixReturnsWatcher() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var watch = mock(Watch.class);
        var watcher = mock(Watch.Watcher.class);
        when(watch.watch(
                any(ByteSequence.class),
                any(WatchOption.class),
                any(TypedWatchListenerAdapter.class)
        )).thenReturn(watcher);

        var accessor = accessor(kv, watch);
        assertSame(watcher, accessor.watchPrefix("p", mockObserver()));
    }

    @Test
    void putBatchSucceedsAndReturnsRevision() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var txn = txnReturning(txnSucceeded(8L));
        when(kv.txn()).thenReturn(txn);

        var accessor = accessor(kv, mock(Watch.class));
        assertEquals(8L, accessor.putBatch(List.of(Put.of("k", "v"))).getAsLong());
    }

    @Test
    void putBatchEmptyReturnsEmptyWithoutTransaction() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);

        var accessor = accessor(kv, mock(Watch.class));
        assertTrue(accessor.putBatch(List.of()).isEmpty());
        verify(kv, never()).txn();
    }

    @Test
    void casBatchReturnsRevisionOnSuccess() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var txn = txnReturning(txnSucceeded(9L));
        when(kv.txn()).thenReturn(txn);

        var accessor = accessor(kv, mock(Watch.class));
        var result = accessor.casBatch(List.of(KVRecord.ofGeneric("k", "v", 1L)));
        assertEquals(9L, result.getAsLong());
    }

    @Test
    void casWithSidePutsDelegatesToBatchTransaction() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var txn = txnReturning(txnSucceeded(10L));
        when(kv.txn()).thenReturn(txn);

        var accessor = accessor(kv, mock(Watch.class));
        var result = accessor.cas("k", "v", 1L, SidePut.of(TestCodecs.stringKVCodec(), "sk", "sv"));
        assertEquals(10L, result.getAsLong());
    }

    @Test
    void putIfAbsentWithLeaseReturnsRevision() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        var txn = txnReturning(txnSucceeded(6L));
        when(kv.txn()).thenReturn(txn);

        var accessor = accessor(kv, mock(Watch.class));
        assertEquals(6L, accessor.putIfAbsentWithLease("k", "v", 1L).getAsLong());
    }

    @Test
    void getUnwrapsTransportFailure() {
        var kv = mock(KV.class, RETURNS_DEEP_STUBS);
        when(kv.get(any(ByteSequence.class)))
                .thenReturn(CompletableFuture.failedFuture(
                        new ExecutionException(new IllegalStateException("transport down"))));

        var accessor = accessor(kv, mock(Watch.class));
        assertThatThrownBy(() -> accessor.get("k"))
                .isInstanceOf(RuntimeException.class);
    }
}
