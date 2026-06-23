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

class RequestPacketTest {

    private static final JavaType STRING_TYPE = JsonCodec.resolveType(String.class);

    @Test
    void buildAndRead() {
        var id = Id.of(1);
        var request = new Request(id, "test", null);
        var future = new CompletableFuture<String>();

        var packet = RequestPacket.<String>builder()
                .id(id)
                .request(request)
                .future(future)
                .resultType(STRING_TYPE)
                .postedAt(1000)
                .timeoutAt(5000)
                .build();

        assertEquals(id, packet.id());
        assertEquals(request, packet.request());
        assertEquals(future, packet.future());
        assertEquals(STRING_TYPE, packet.resultType());
        assertEquals(1000, packet.postedAt());
        assertEquals(5000, packet.timeoutAt());
    }

    @Test
    void defaultTimesAreZero() {
        var id = Id.of(2);
        var request = new Request(id, "method", null);
        var future = new CompletableFuture<String>();

        var packet = RequestPacket.<String>builder()
                .id(id)
                .request(request)
                .future(future)
                .resultType(STRING_TYPE)
                .build();

        assertEquals(0, packet.postedAt());
        assertEquals(0, packet.timeoutAt());
    }

    @Test
    void buildFailsWithNullId() {
        assertThrows(NullPointerException.class, () ->
                RequestPacket.<String>builder()
                        .request(new Request(Id.of(1), "test", null))
                        .future(new CompletableFuture<>())
                        .resultType(STRING_TYPE)
                        .build()
        );
    }

    @Test
    void buildFailsWithNullRequest() {
        assertThrows(NullPointerException.class, () ->
                RequestPacket.<String>builder()
                        .id(Id.of(1))
                        .future(new CompletableFuture<>())
                        .resultType(STRING_TYPE)
                        .build()
        );
    }

    @Test
    void buildFailsWithNullFuture() {
        assertThrows(NullPointerException.class, () ->
                RequestPacket.<String>builder()
                        .id(Id.of(1))
                        .request(new Request(Id.of(1), "test", null))
                        .resultType(STRING_TYPE)
                        .build()
        );
    }

    @Test
    void equalsAndHashCode() {
        var id = Id.of(1);
        var req = new Request(id, "m", null);
        var future = new CompletableFuture<String>();
        var p1 = RequestPacket.<String>builder()
                .id(id).request(req).future(future).resultType(STRING_TYPE)
                .postedAt(100).timeoutAt(200).build();
        var p2 = RequestPacket.<String>builder()
                .id(id).request(req).future(future).resultType(STRING_TYPE)
                .postedAt(100).timeoutAt(200).build();
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void notEqualsDifferentId() {
        var req = new Request(Id.of(1), "m", null);
        var future = new CompletableFuture<String>();
        var p1 = RequestPacket.<String>builder()
                .id(Id.of(1)).request(req).future(future).resultType(STRING_TYPE).build();
        var p2 = RequestPacket.<String>builder()
                .id(Id.of(2)).request(req).future(future).resultType(STRING_TYPE).build();
        assertNotEquals(p1, p2);
    }
}
