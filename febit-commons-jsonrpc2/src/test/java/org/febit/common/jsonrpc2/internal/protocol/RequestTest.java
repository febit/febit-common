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
package org.febit.common.jsonrpc2.internal.protocol;

import org.febit.common.jsonrpc2.protocol.IRpcRequest;
import org.febit.common.jsonrpc2.protocol.Id;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {

    @Test
    void basicFields() {
        var id = Id.of(1);
        var req = new Request(id, "calc/add", List.of(1, 2));
        assertEquals(id, req.id());
        assertEquals("calc/add", req.method());
        assertEquals(List.of(1, 2), req.params());
    }

    @Test
    void nullParams() {
        var id = Id.of("req-1");
        var req = new Request(id, "ping", null);
        assertEquals(id, req.id());
        assertEquals("ping", req.method());
        assertNull(req.params());
    }

    @Test
    void implementsIRpcRequest() {
        var req = new Request(Id.of(1), "test", null);
        assertInstanceOf(IRpcRequest.class, req);
    }

    @Test
    void equalsAndHashCode() {
        var id = Id.of(1);
        var r1 = new Request(id, "m", "p");
        var r2 = new Request(id, "m", "p");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void notEqualsDifferentId() {
        var r1 = new Request(Id.of(1), "m", "p");
        var r2 = new Request(Id.of(2), "m", "p");
        assertNotEquals(r1, r2);
    }

    @Test
    void notEqualsDifferentMethod() {
        var id = Id.of(1);
        var r1 = new Request(id, "a", "p");
        var r2 = new Request(id, "b", "p");
        assertNotEquals(r1, r2);
    }

    @Test
    void notEqualsDifferentParams() {
        var id = Id.of(1);
        var r1 = new Request(id, "m", "a");
        var r2 = new Request(id, "m", "b");
        assertNotEquals(r1, r2);
    }

    @Test
    void toStringContainsFields() {
        var req = new Request(Id.of("abc"), "test/method", null);
        var str = req.toString();
        assertTrue(str.contains("abc"));
        assertTrue(str.contains("test/method"));
    }
}
