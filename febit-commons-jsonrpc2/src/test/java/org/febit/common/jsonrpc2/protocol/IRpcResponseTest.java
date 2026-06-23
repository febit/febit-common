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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IRpcResponseTest {

    private record TestRpcResponse<T>(
            @NonNull Id id,
            @Nullable T result,
            @Nullable IRpcError<?> error,
            @NonNull String jsonrpc
    ) implements IRpcResponse<T> {
    }

    @Test
    void hasErrorReturnsTrueWhenErrorPresent() {
        var error = StdRpcErrors.INVALID_REQUEST.toError();
        var resp = new TestRpcResponse<>(Id.of(1), null, error, "2.0");
        assertTrue(resp.hasError());
    }

    @Test
    void hasErrorReturnsFalseWhenErrorIsNull() {
        var resp = new TestRpcResponse<>(Id.of(1), "result", null, "2.0");
        assertFalse(resp.hasError());
    }

    @Test
    void resultAvailableWhenNoError() {
        var resp = new TestRpcResponse<>(Id.of(42L), "hello world", null, "2.0");
        assertEquals("hello world", resp.result());
        assertFalse(resp.hasError());
    }

    @Test
    void errorCodeAndMessageAccessible() {
        var error = StdRpcErrors.METHOD_NOT_FOUND.toError();
        var resp = new TestRpcResponse<>(Id.of("abc"), null, error, "2.0");
        assertTrue(resp.hasError());
        assertEquals(StdRpcErrors.METHOD_NOT_FOUND.code(), resp.error().code());
    }

    @Test
    void idIsRequiredAndNonNull() {
        var id = Id.of("test-id");
        var resp = new TestRpcResponse<>(id, null, null, "2.0");
        assertEquals(id, resp.id());
    }

    @Test
    void jsonrpcValueIs20() {
        var resp = new TestRpcResponse<>(Id.of(1), "ok", null, "2.0");
        assertEquals("2.0", resp.jsonrpc());
    }
}
