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

import org.febit.common.jsonrpc2.protocol.Id;
import org.febit.common.jsonrpc2.protocol.StdRpcErrors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void okWithResult() {
        var id = Id.of(1);
        var resp = Response.ok(id, "result-value");
        assertEquals(id, resp.id());
        assertEquals("result-value", resp.result());
        assertNull(resp.error());
    }

    @Test
    void okWithNullResult() {
        var id = Id.of("abc");
        var resp = Response.ok(id, null);
        assertEquals(id, resp.id());
        assertNull(resp.result());
        assertNull(resp.error());
    }

    @Test
    void okWithoutResult() {
        var id = Id.of(42L);
        var resp = Response.<String>ok(id);
        assertEquals(id, resp.id());
        assertNull(resp.result());
        assertNull(resp.error());
    }

    @Test
    void failed() {
        var id = Id.of(1);
        var error = StdRpcErrors.METHOD_NOT_FOUND.toError();
        var resp = Response.failed(id, error);
        assertEquals(id, resp.id());
        assertNull(resp.result());
        assertEquals(error, resp.error());
    }

    @Test
    void failedRejectsNullError() {
        var id = Id.of(1);
        assertThrows(NullPointerException.class, () -> Response.failed(id, null));
    }

    @Test
    void okResponseEquality() {
        var id = Id.of(1);
        var r1 = Response.ok(id, "value");
        var r2 = Response.ok(Id.of(1), "value");
        assertEquals(r1, r2);
    }

    @Test
    void failedResponseEquality() {
        var id = Id.of(1);
        var error = StdRpcErrors.INTERNAL_ERROR.toError();
        var r1 = Response.failed(id, error);
        var r2 = Response.failed(Id.of(1), error);
        assertEquals(r1, r2);
    }

    @Test
    void hasErrorReturnsTrueWhenErrorIsPresent() {
        var resp = Response.failed(Id.of(1), StdRpcErrors.INVALID_REQUEST.toError());
        assertTrue(resp.hasError());
    }

    @Test
    void hasErrorReturnsFalseWhenNoError() {
        var resp = Response.ok(Id.of(1), "value");
        assertFalse(resp.hasError());
    }

    @Test
    void toStringContainsFields() {
        var resp = Response.ok(Id.of(42), "hello");
        var str = resp.toString();
        assertTrue(str.contains("42"));
        assertTrue(str.contains("hello"));
    }

    @Test
    void notEqualsDifferentResult() {
        var r1 = Response.ok(Id.of(1), "a");
        var r2 = Response.ok(Id.of(1), "b");
        assertNotEquals(r1, r2);
    }

    @Test
    void notEqualsDifferentError() {
        var r1 = Response.failed(Id.of(1), StdRpcErrors.INVALID_REQUEST.toError());
        var r2 = Response.failed(Id.of(1), StdRpcErrors.INTERNAL_ERROR.toError());
        assertNotEquals(r1, r2);
    }

    @Test
    void okWithoutResultEquality() {
        var r1 = Response.<String>ok(Id.of(1));
        var r2 = Response.<String>ok(Id.of(1));
        assertEquals(r1, r2);
    }
}
