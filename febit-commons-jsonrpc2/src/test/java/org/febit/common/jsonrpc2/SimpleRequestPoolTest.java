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
package org.febit.common.jsonrpc2;

import org.febit.common.jsonrpc2.internal.protocol.Request;
import org.febit.common.jsonrpc2.protocol.Id;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JavaType;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class SimpleRequestPoolTest {

    private static final JavaType STRING_TYPE = JsonCodec.resolveType(String.class);

    private static RequestPacket<String> createPacket(Id id) {
        return RequestPacket.<String>builder()
                .id(id)
                .request(new Request(id, "test", null))
                .future(new CompletableFuture<>())
                .resultType(STRING_TYPE)
                .postedAt(0)
                .timeoutAt(-1)
                .build();
    }

    @Test
    void addAndPop() {
        var pool = new SimpleRequestPool();
        var id = Id.of("req-1");
        var packet = createPacket(id);

        pool.add(packet);
        var popped = pool.pop(id);
        assertSame(packet, popped);
    }

    @Test
    void popNonexistentReturnsNull() {
        var pool = new SimpleRequestPool();
        assertNull(pool.pop(Id.of("missing")));
    }

    @Test
    void popRemovesEntry() {
        var pool = new SimpleRequestPool();
        var id = Id.of("req-1");
        pool.add(createPacket(id));

        assertNotNull(pool.pop(id));
        assertNull(pool.pop(id), "Second pop should return null");
    }

    @Test
    void multipleEntries() {
        var pool = new SimpleRequestPool();
        var id1 = Id.of("a");
        var id2 = Id.of("b");
        var p1 = createPacket(id1);
        var p2 = createPacket(id2);

        pool.add(p1);
        pool.add(p2);

        assertSame(p2, pool.pop(id2));
        assertSame(p1, pool.pop(id1));
    }

    @Test
    void sameIdOverwrites() {
        var pool = new SimpleRequestPool();
        var id = Id.of("same");
        var p1 = createPacket(id);
        var p2 = createPacket(id);
        pool.add(p1);
        pool.add(p2);

        var popped = pool.pop(id);
        assertSame(p2, popped);
    }

    @Test
    void popFromEmptyPool() {
        var pool = new SimpleRequestPool();
        assertNull(pool.pop(Id.of(1)));
        assertNull(pool.pop(Id.of("abc")));
    }
}
