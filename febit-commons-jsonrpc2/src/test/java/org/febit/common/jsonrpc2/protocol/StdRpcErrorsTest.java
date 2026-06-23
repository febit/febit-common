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
package org.febit.common.jsonrpc2.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StdRpcErrorsTest {

    @Test
    void parseError() {
        assertEquals(-32700, StdRpcErrors.PARSE_ERROR.code());
        assertEquals("Parse error", StdRpcErrors.PARSE_ERROR.message());
        assertTrue(StdRpcErrors.PARSE_ERROR.description().contains("Invalid JSON"));
    }

    @Test
    void invalidRequest() {
        assertEquals(-32600, StdRpcErrors.INVALID_REQUEST.code());
        assertEquals("Invalid Request", StdRpcErrors.INVALID_REQUEST.message());
        assertTrue(StdRpcErrors.INVALID_REQUEST.description().contains("valid Request"));
    }

    @Test
    void methodNotFound() {
        assertEquals(-32601, StdRpcErrors.METHOD_NOT_FOUND.code());
        assertEquals("Method not found", StdRpcErrors.METHOD_NOT_FOUND.message());
        assertTrue(StdRpcErrors.METHOD_NOT_FOUND.description().contains("not exist"));
    }

    @Test
    void invalidParams() {
        assertEquals(-32602, StdRpcErrors.INVALID_PARAMS.code());
        assertEquals("Invalid params", StdRpcErrors.INVALID_PARAMS.message());
        assertTrue(StdRpcErrors.INVALID_PARAMS.description().contains("Invalid method parameter"));
    }

    @Test
    void internalError() {
        assertEquals(-32603, StdRpcErrors.INTERNAL_ERROR.code());
        assertEquals("Internal error", StdRpcErrors.INTERNAL_ERROR.message());
        assertTrue(StdRpcErrors.INTERNAL_ERROR.description().contains("Internal JSON-RPC error"));
    }

    @Test
    void toErrorDefault() {
        var error = StdRpcErrors.PARSE_ERROR.toError();
        assertEquals(-32700, error.code());
        assertEquals("Parse error", error.message());
        assertNull(error.data());
    }

    @Test
    void toErrorWithMessage() {
        var error = StdRpcErrors.INVALID_REQUEST.toError("custom msg");
        assertEquals(-32600, error.code());
        assertEquals("custom msg", error.message());
        assertNull(error.data());
    }

    @Test
    void toErrorWithData() {
        var error = StdRpcErrors.INTERNAL_ERROR.toError("internal", "data-value");
        assertEquals(-32603, error.code());
        assertEquals("internal", error.message());
        assertEquals("data-value", error.data());
    }

    @Test
    void toExceptionWithoutCause() {
        var ex = StdRpcErrors.METHOD_NOT_FOUND.toException("not found");
        assertEquals(-32601, ex.getError().code());
        assertEquals("not found", ex.getError().message());
    }

    @Test
    void toExceptionWithCause() {
        var cause = new RuntimeException("root cause");
        var ex = StdRpcErrors.INTERNAL_ERROR.toException("failed", cause);
        assertEquals(-32603, ex.getError().code());
        assertEquals("failed", ex.getError().message());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void toExceptionWithData() {
        var ex = StdRpcErrors.INVALID_PARAMS.toException("bad", "detail");
        assertEquals(-32602, ex.getError().code());
        assertEquals("bad", ex.getError().message());
        assertEquals("detail", ex.getError().data());
    }

    @Test
    void toExceptionDefault() {
        var ex = StdRpcErrors.PARSE_ERROR.toException();
        assertEquals(-32700, ex.getError().code());
    }
}
