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
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.Thread.currentThread;
import static org.febit.common.etcd.store.codec.CodecUtils.bytes;

/**
 * Auto-paginating scanner over a key range in etcd.
 *
 * <p>Yields raw {@link KeyValue} items. Each call to {@link #hasNext()} triggers a
 * page fetch when the internal buffer is exhausted, transparently advancing the
 * scan cursor via {@link #incrementKey(ByteSequence)}.
 *
 * <p>Instances are created via static factory methods:
 * <ul>
 *   <li>{@link #ofPrefix(Client, String, int)} — prefix-based scan</li>
 *   <li>{@link #ofRange(Client, String, String, int)} - explicit range scan</li>
 * </ul>
 *
 * <p>An optional {@code limit} caps the total number of yielded items.
 */
@Slf4j
public class EtcdPagingScanner implements Iterator<KeyValue> {

    private final Client client;
    private final ByteSequence rangeFrom;
    private final ByteSequence rangeEnd;
    private final int fetchSize;

    private final Deque<KeyValue> buffer = new ArrayDeque<>();
    private @Nullable ByteSequence nextFromKey;
    private long remaining;
    private boolean exhausted;

    private EtcdPagingScanner(
            Client client,
            ByteSequence rangeFrom,
            ByteSequence rangeEnd,
            int fetchSize,
            long limit
    ) {
        this.client = client;
        this.rangeFrom = rangeFrom;
        this.rangeEnd = rangeEnd;
        this.fetchSize = fetchSize;
        this.remaining = limit;
    }

    /**
     * Creates a prefix-based scanner.
     *
     * @param client    etcd client
     * @param prefix    key prefix to scan
     * @param fetchSize number of keys per etcd fetch
     */
    public static EtcdPagingScanner ofPrefix(Client client, String prefix, int fetchSize) {
        return new EtcdPagingScanner(client, bytes(prefix), prefixEndOf(prefix), fetchSize, Long.MAX_VALUE);
    }

    /**
     * Creates a prefix-based scanner with a total item limit.
     *
     * @param client    etcd client
     * @param prefix    key prefix to scan
     * @param fetchSize number of keys per etcd fetch
     * @param limit     maximum total items to yield
     */
    public static EtcdPagingScanner ofPrefix(Client client, String prefix, int fetchSize, long limit) {
        return new EtcdPagingScanner(client, bytes(prefix), prefixEndOf(prefix), fetchSize, limit);
    }

    /**
     * Creates a range-based scanner without a total item limit.
     *
     * @param client    etcd client
     * @param rangeFrom inclusive start key
     * @param rangeEnd  exclusive end key
     * @param fetchSize number of keys per etcd fetch
     */
    public static EtcdPagingScanner ofRange(Client client, String rangeFrom, String rangeEnd, int fetchSize) {
        return new EtcdPagingScanner(client, bytes(rangeFrom), bytes(rangeEnd), fetchSize, Long.MAX_VALUE);
    }

    /**
     * Creates a range-based scanner with a total item limit.
     *
     * @param client    etcd client
     * @param rangeFrom inclusive start key
     * @param rangeEnd  exclusive end key
     * @param fetchSize number of keys per etcd fetch
     * @param limit     maximum total items to yield
     */
    public static EtcdPagingScanner ofRange(Client client, String rangeFrom, String rangeEnd,
                                            int fetchSize, long limit) {
        return new EtcdPagingScanner(client, bytes(rangeFrom), bytes(rangeEnd), fetchSize, limit);
    }

    /**
     * Computes the range-end (exclusive upper-bound) for a prefix scan.
     *
     * <p>All keys that start with {@code prefix} form the lexicographic range
     * {@code [prefix, prefixEnd)}. The end key is the smallest key strictly greater
     * than every key having {@code prefix} as a prefix, obtained by incrementing the
     * last byte of the prefix (carrying leftward).
     *
     * <p>Appending a {@code \0} byte would be incorrect: it would exclude any key
     * whose next byte is greater than {@code 0x00} (i.e. virtually all real keys).
     */
    public static ByteSequence prefixEndOf(String prefix) {
        var bytes = prefix.getBytes(StandardCharsets.UTF_8);
        var endBytes = new byte[bytes.length];
        System.arraycopy(bytes, 0, endBytes, 0, bytes.length);
        for (int i = endBytes.length - 1; i >= 0; i--) {
            if (endBytes[i] != (byte) 0xFF) {
                endBytes[i] = (byte) (endBytes[i] + 1);
                break;
            }
            endBytes[i] = 0;
        }
        return ByteSequence.from(endBytes);
    }

    /**
     * Returns a {@link ByteSequence} that is lexicographically one greater than the given key.
     */
    public static ByteSequence incrementKey(ByteSequence key) {
        var bytes = key.getBytes();
        var nextBytes = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, nextBytes, 0, bytes.length);
        return ByteSequence.from(nextBytes);
    }

    public Stream<KeyValue> stream() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED), false);
    }

    @Override
    public boolean hasNext() {
        if (!buffer.isEmpty()) {
            return true;
        }
        if (exhausted || remaining <= 0) {
            return false;
        }
        fetchPage();
        return !buffer.isEmpty();
    }

    @Override
    public KeyValue next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        var kv = buffer.poll();
        Objects.requireNonNull(kv);
        return kv;
    }

    void fetchPage() {
        var startKey = nextFromKey != null ? nextFromKey : rangeFrom;
        var opt = GetOption.builder()
                .withRange(rangeEnd)
                .withLimit(Math.min(fetchSize, remaining))
                .build();
        GetResponse resp;
        try {
            resp = client.getKVClient().get(startKey, opt).get();
        } catch (InterruptedException e) {
            currentThread().interrupt();
            log.error("[Etcd] Paging scan interrupted from key={}", startKey);
            exhausted = true;
            return;
        } catch (Exception e) {
            log.error("[Etcd] Failed to scan page from key={}", startKey, e);
            exhausted = true;
            return;
        }
        var kvs = resp.getKvs();
        if (kvs.isEmpty()) {
            exhausted = true;
            return;
        }
        buffer.addAll(kvs);
        remaining -= kvs.size();
        nextFromKey = incrementKey(kvs.getLast().getKey());
    }
}
