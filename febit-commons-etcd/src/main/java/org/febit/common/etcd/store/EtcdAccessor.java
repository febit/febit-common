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
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.febit.common.etcd.store.codec.CodecUtils;
import org.febit.common.etcd.store.codec.KVCodec;
import org.febit.lang.util.Lists;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Typed accessor over Etcd KV operations: get, put, delete, CAS, and watch.
 *
 * <p>Binds a {@link KVCodec} (key encoding + value encoding) to the etcd client, so
 * callers work with typed objects ({@code K}, {@code V}) instead of raw {@code ByteSequence}.
 *
 * <h3>Optimistic locking</h3>
 * <p>{@link #get(K)} returns the value with its {@code modRevision}. Pass that revision to
 * {@link #cas(K, V, long)} — the write only succeeds if no other writer modified the key.
 * {@link #casBatch(List, SidePut[])} extends this to multiple keys in a single atomic transaction.
 *
 * <p>{@link #putIfAbsent(K, V, SidePut[])} uses {@code version == 0} as the guard: atomically checks
 * that the key does not exist before writing. This is the basis for heartbeat lease creation.
 *
 * <h3>Side-put</h3>
 * <p>{@link #cas(K, V, long, SidePut[])} and {@link #putIfAbsentWithLease(K, V, long, SidePut[])}
 * atomically write additional keys (different types) in the same transaction &mdash; for example,
 * writing a running marker when transitioning pipeline state.
 *
 * <h3>Quick start</h3>
 * <pre>{@code
 * // Read → modify → CAS
 * var entry = accessor.get(stub);
 * if (entry.isPresent()) {
 *     var next = entry.get().value().transitionToRunning();
 *     accessor.cas(stub, next, entry.get().revision());
 * }
 *
 * // putIfAbsent with lease (heartbeat)
 * accessor.putIfAbsentWithLease(stub, heartbeat, leaseId);
 * }</pre>
 *
 * @param <K> key type, typically a Sub record
 * @param <V> value type, typically a state record
 */
@Slf4j
@lombok.Builder(
        builderClassName = "Builder"
)
@Accessors(fluent = true)
public class EtcdAccessor<K, V> {

    @lombok.Getter
    @lombok.NonNull
    private final KV kv;
    @lombok.Getter
    @lombok.NonNull
    private final Watch watch;
    @lombok.Getter
    @lombok.NonNull
    private final KVCodec<K, V> codec;

    @lombok.NonNull
    private final EtcdMetrics metrics;

    private Optional<KVRecord<K, V>> decode(@Nullable KeyValue src) {
        if (src == null) {
            return Optional.empty();
        }
        var key = codec.key().decode(src.getKey());
        if (key == null) {
            metrics.error().keyParse();
            return Optional.empty();
        }
        var value = codec.value().decode(src.getValue());
        if (value == null) {
            log.warn("[Etcd] Empty or unparseable value key={}", key);
            metrics.error().emptyValue();
            return Optional.empty();
        }
        return Optional.of(
                KVRecord.ofGeneric(key, value, src.getModRevision())
        );
    }

    /**
     * Reads a single key, returning the decoded record with its {@code modRevision}.
     *
     * <p>The revision is the optimistic-lock token for {@link #cas(K, V, long)}.
     *
     * @return the record, or empty if the key is absent
     */
    @SuppressWarnings({
            "java:S2142" // "InterruptedException" and "ThreadDeath" should not be ignored
    })
    public Optional<KVRecord<K, V>> get(K key) {
        GetResponse resp;
        try {
            resp = kv.get(codec.key().encode(key)).get();
        } catch (Exception e) {
            metrics.error().record(e);
            throw ExceptionUtils.unwrap(e, "Failed to get key: " + key);
        }
        if (resp.getCount() == 0) {
            return Optional.empty();
        }
        return decode(
                resp.getKvs().getFirst()
        );
    }

    /**
     * Lists all key-value pairs under the given prefix.
     *
     * <p>Keys that do not match the codec's key format are silently skipped.
     * Only matching keys trigger value deserialization.
     */
    @SuppressWarnings({
            "java:S2142" // "InterruptedException" and "ThreadDeath" should not be ignored
    })
    public List<KVRecord<K, V>> listWithPrefix(String prefix) {
        var encodedPrefix = CodecUtils.bytes(prefix);
        var opt = GetOption.builder()
                .isPrefix(true)
                .build();
        GetResponse resp;
        try {
            resp = kv.get(encodedPrefix, opt).get();
        } catch (Exception e) {
            metrics.error().record(e);
            throw ExceptionUtils.unwrap(e, "Failed to get prefix: " + prefix);
        }
        return resp.getKvs().stream()
                .map(this::decode)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * Lists all decoded keys under the given prefix (values are not fetched).
     */
    @SuppressWarnings({
            "java:S2142" // "InterruptedException" and "ThreadDeath" should not be ignored
    })
    public List<K> listKeys(String prefix) {
        var encodedPrefix = CodecUtils.bytes(prefix);
        var opt = GetOption.builder()
                .isPrefix(true)
                .withKeysOnly(true)
                .build();
        GetResponse resp;
        try {
            resp = kv.get(encodedPrefix, opt).get();
        } catch (Exception e) {
            metrics.error().record(e);
            throw ExceptionUtils.unwrap(e, "Failed to list keys: " + prefix);
        }
        return resp.getKvs().stream()
                .map(e -> codec.key().decode(e.getKey()))
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Prefix GET that also returns the header revision of this read.
     *
     * <p>Use the returned revision as the gate baseline when reconciling a projection:
     * only drop projection rows whose {@code etcd_revision} is older than this read.
     */
    @SuppressWarnings({
            "java:S2142" // "InterruptedException" and "ThreadDeath" should not be ignored
    })
    public PrefixScan<K, V> listWithPrefixWithRevision(String prefix) {
        var encodedPrefix = CodecUtils.bytes(prefix);
        var opt = GetOption.builder()
                .isPrefix(true)
                .build();
        GetResponse resp;
        try {
            resp = kv.get(encodedPrefix, opt).get();
        } catch (Exception e) {
            metrics.error().record(e);
            throw ExceptionUtils.unwrap(e, "Failed to get prefix: " + prefix);
        }
        var records = resp.getKvs().stream()
                .map(this::decode)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        return new PrefixScan<>(resp.getHeader().getRevision(), records);
    }

    /**
     * Counts keys under the given prefix (only the count is fetched, not keys or values).
     *
     * @param prefix the prefix to count under
     * @return number of keys matching the prefix
     */
    @SuppressWarnings({
            "java:S2142" // "InterruptedException" and "ThreadDeath" should not be ignored
    })
    public long countWithPrefix(String prefix) {
        var encodedPrefix = CodecUtils.bytes(prefix);
        var opt = GetOption.builder()
                .isPrefix(true)
                .withCountOnly(true)
                .build();
        GetResponse resp;
        try {
            resp = kv.get(encodedPrefix, opt).get();
        } catch (Exception e) {
            metrics.error().record(e);
            throw ExceptionUtils.unwrap(e, "Failed to count prefix: " + prefix);
        }
        return resp.getCount();
    }

    /**
     * Compare-and-swap: writes {@code value} only if the key's current {@code modRevision}
     * equals {@code revision}.
     *
     * @return the new revision on success, {@link OptionalLong#empty()} on conflict
     */
    @SuppressWarnings({
            "java:S2142" // "InterruptedException" and "ThreadDeath" should not be ignored
    })
    public OptionalLong cas(K key, V value, long revision) {
        var encodedKey = codec.key().encode(key);
        var encodedValue = codec.value().encode(value);
        var txn = kv.txn()
                .If(new Cmp(encodedKey, Cmp.Op.EQUAL, CmpTarget.modRevision(revision)))
                .Then(Op.put(encodedKey, encodedValue, PutOption.DEFAULT))
                .Else(Op.get(encodedKey, GetOption.DEFAULT));
        TxnResponse resp;
        try {
            resp = txn.commit().get();
        } catch (Exception e) {
            metrics.cas().error();
            metrics.error().record(e);
            throw ExceptionUtils.unwrap(e, "CAS failed for key: " + key);
        }
        if (resp.isSucceeded()) {
            metrics.cas().success();
            return OptionalLong.of(resp.getHeader().getRevision());
        }
        log.debug("[Etcd] CAS failed for key={}, expectedRev={}", key, revision);
        metrics.cas().conflict();
        return OptionalLong.empty();
    }

    /**
     * CAS with atomic side-puts. Writes the main key + all {@link SidePut side-puts}
     * in a single transaction, only if the main key's {@code modRevision} matches.
     *
     * @param key      main key
     * @param value    main value
     * @param revision expected modRevision
     * @param sidePuts additional cross-type writes (varargs)
     * @return the new revision on success, {@link OptionalLong#empty()} on conflict
     */
    public OptionalLong cas(K key, V value, long revision, SidePut<?, ?>... sidePuts) {
        return putBatch(List.of(Put.ofCas(key, value, revision)), sidePuts);
    }

    /**
     * Batch CAS: checks the expected revision for every entry, then atomically
     * writes all of them.
     *
     * @param entries list of (key, value, expectedRevision) tuples
     * @return the new revision on success, {@link OptionalLong#empty()} if any check fails
     */
    public <E extends KVRecord<K, V>> OptionalLong casBatch(List<E> entries, SidePut<?, ?>... sidePuts) {
        return put0(
                Lists.collect(entries, Put::ofCas),
                PutOption.DEFAULT,
                sidePuts
        );
    }

    /**
     * Atomically writes {@code value} only if the key does not yet exist (version == 0).
     *
     * @return the new revision, or empty if the key already exists
     */
    public OptionalLong putIfAbsent(K key, V value, SidePut<?, ?>... sidePuts) {
        return put0(List.of(Put.ofIfAbsent(key, value)), PutOption.DEFAULT, sidePuts);
    }

    /**
     * Atomically writes {@code value} only if the key does not yet exist, bound to the given lease.
     *
     * @return the new revision, or empty if the key already exists
     */
    public OptionalLong putIfAbsentWithLease(
            K key, V value, long leaseId, SidePut<?, ?>... sidePuts) {
        var opt = PutOption.builder().withLeaseId(leaseId).build();
        return put0(List.of(Put.ofIfAbsent(key, value)), opt, sidePuts);
    }

    /**
     * Unconditional put &mdash; no version check. Useful for initial writes.
     *
     * @return the new modRevision
     */
    @SuppressWarnings({
            "java:S2142" // "InterruptedException" and "ThreadDeath" should not be ignored
    })
    public long put(K key, V value) {
        var encodedKey = codec.key().encode(key);
        var encodedValue = codec.value().encode(value);
        PutResponse resp;
        try {
            resp = kv.put(encodedKey, encodedValue).get();
        } catch (Exception e) {
            metrics.error().record(e);
            throw ExceptionUtils.unwrap(e, "Failed to put key: " + key);
        }
        return resp.getHeader().getRevision();
    }

    /**
     * Unconditional put, bound to the given lease.
     *
     * @return the new modRevision
     */
    @SuppressWarnings({
            "java:S2142" // "InterruptedException" and "ThreadDeath" should not be ignored
    })
    public long putWithLease(K key, V value, long leaseId) {
        var encodedKey = codec.key().encode(key);
        var encodedValue = codec.value().encode(value);
        var opt = PutOption.builder().withLeaseId(leaseId).build();
        PutResponse resp;
        try {
            resp = kv.put(encodedKey, encodedValue, opt).get();
        } catch (Exception e) {
            metrics.error().record(e);
            throw ExceptionUtils.unwrap(e, "Failed to put with lease: " + key);
        }
        return resp.getHeader().getRevision();
    }

    /**
     * Atomically applies all puts (and optional side-puts) in one transaction.
     *
     * @return the new revision, or empty if any conditional put conflicts
     */
    public OptionalLong putBatch(List<Put<K, V>> entries, SidePut<?, ?>... sidePuts) {
        return put0(entries, PutOption.DEFAULT, sidePuts);
    }

    /**
     * Atomically applies all puts (and optional side-puts) in one transaction, bound to the given lease.
     *
     * @return the new revision, or empty if any conditional put conflicts
     */
    public OptionalLong putBatchWithLease(
            List<Put<K, V>> entries, long leaseId, SidePut<?, ?>... sidePuts) {
        var opt = PutOption.builder().withLeaseId(leaseId).build();
        return put0(entries, opt, sidePuts);
    }

    /**
     * Core batch put logic: builds conditions for IfAbsent/CAS, then commits
     * a single transaction atomically with optional cross-type {@link SidePut side-puts}.
     */
    @SuppressWarnings({
            "java:S2142" // "InterruptedException" and "ThreadDeath" should not be ignored
    })
    private OptionalLong put0(List<Put<K, V>> puts, PutOption opt, SidePut<?, ?>... sidePuts) {
        if (puts.isEmpty() && sidePuts.length == 0) {
            return OptionalLong.empty();
        }
        var txn = kv.txn();
        var ops = new Op[puts.size() + sidePuts.length];
        var hasConditions = false;

        for (int i = 0; i < puts.size(); i++) {
            var e = puts.get(i);
            var key = codec.key().encode(e.key());
            var value = codec.value().encode(e.value());
            ops[i] = Op.put(key, value, opt);

            switch (e) {
                case Put.Overwrite<K, V> v -> {
                }
                case Put.IfAbsent<K, V> v -> {
                    txn.If(new Cmp(key, Cmp.Op.EQUAL, CmpTarget.version(0)));
                    hasConditions = true;
                }
                case Put.Cas<K, V> cas -> {
                    txn.If(new Cmp(key, Cmp.Op.EQUAL, CmpTarget.modRevision(cas.modRevision())));
                    hasConditions = true;
                }
            }
        }

        for (int i = 0; i < sidePuts.length; i++) {
            var s = sidePuts[i];
            var key = s.encodedKey();
            var value = s.encodedValue();
            ops[puts.size() + i] = Op.put(key, value, PutOption.DEFAULT);
            switch (s) {
                case SidePut.Overwrite<?, ?> v -> {
                }
                case SidePut.IfAbsent<?, ?> v -> {
                    txn.If(new Cmp(key, Cmp.Op.EQUAL, CmpTarget.version(0)));
                    hasConditions = true;
                }
                case SidePut.Cas<?, ?> cas -> {
                    txn.If(new Cmp(key, Cmp.Op.EQUAL, CmpTarget.modRevision(cas.modRevision())));
                    hasConditions = true;
                }
            }
        }

        txn.Then(ops);
        if (hasConditions) {
            var firstKey = puts.isEmpty()
                    ? sidePuts[0].encodedKey()
                    : codec.key().encode(puts.getFirst().key());
            txn.Else(Op.get(firstKey, GetOption.DEFAULT));
        }

        TxnResponse resp;
        try {
            resp = txn.commit().get();
        } catch (Exception e) {
            metrics.error().record(e);
            throw ExceptionUtils.unwrap(e, "Batch put failed");
        }
        if (resp.isSucceeded()) {
            metrics.cas().batchSuccess();
            return OptionalLong.of(resp.getHeader().getRevision());
        }
        log.debug("[Etcd] Batch put conflict: conditions not met");
        metrics.cas().batchConflict();
        return OptionalLong.empty();
    }

    /**
     * Deletes a single key.
     *
     * @return the number of keys removed (0 or 1)
     */
    @SuppressWarnings({
            "java:S2142" // "InterruptedException" and "ThreadDeath" should not be ignored
    })
    public long delete(K key) {
        var encodedKey = codec.key().encode(key);
        DeleteResponse resp;
        try {
            resp = kv.delete(encodedKey).get();
        } catch (Exception e) {
            metrics.error().record(e);
            throw ExceptionUtils.unwrap(e, "Failed to delete key: " + key);
        }
        return resp.getDeleted();
    }

    /**
     * Deletes all keys under the given prefix.
     *
     * @return number of deleted keys
     */
    @SuppressWarnings({
            "java:S2142" // "InterruptedException" and "ThreadDeath" should not be ignored
    })
    public long deleteWithPrefix(String prefix) {
        var encodedPrefix = CodecUtils.bytes(prefix);
        var opt = DeleteOption.builder().isPrefix(true).build();
        DeleteResponse resp;
        try {
            resp = kv.delete(encodedPrefix, opt).get();
        } catch (Exception e) {
            metrics.error().record(e);
            throw ExceptionUtils.unwrap(e, "Failed to delete prefix: " + prefix);
        }
        log.debug("[Etcd] Deleted {} keys under prefix={}", resp.getDeleted(), prefix);
        return resp.getDeleted();
    }

    /**
     * Watches a single key, delivering decoded typed events (with prevKV) to the observer.
     */
    public Watch.Watcher watch(K key, TypedWatchObserver<K, V> observer) {
        var target = codec.key().encode(key);
        var opt = WatchOption.builder().withPrevKV(true).build();
        var listener = TypedWatchListenerAdapter.of(target, codec, observer, metrics);
        return watch.watch(target, opt, listener);
    }

    /**
     * Watches every key under the prefix, delivering decoded typed events (with prevKV) to the observer.
     */
    public Watch.Watcher watchPrefix(String prefix, TypedWatchObserver<K, V> observer) {
        var target = CodecUtils.bytes(prefix);
        var opt = WatchOption.builder()
                .isPrefix(true)
                .withPrevKV(true)
                .build();
        var listener = TypedWatchListenerAdapter.of(target, codec, observer, metrics);
        return watch.watch(target, opt, listener);
    }

    /**
     * Snapshot of a prefix scan: decoded records plus the header revision of the read.
     *
     * @param revision header revision of the GET, used as the reconcile gate baseline
     * @param records  all decoded KV records under the prefix
     */
    public record PrefixScan<K, V>(long revision, List<KVRecord<K, V>> records) {
    }

}
