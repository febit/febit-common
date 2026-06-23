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
package org.febit.common.jsonrpc2.internal;

import org.febit.common.jsonrpc2.exception.UncheckedRpcException;
import org.febit.common.jsonrpc2.protocol.StdRpcErrors;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class RpcErrorUtilsTest {

    @Test
    void resolveRpcErrorExceptionDirectly() {
        var cause = StdRpcErrors.METHOD_NOT_FOUND.toException();
        var error = RpcErrorUtils.resolveRpcError(cause);
        assertEquals(StdRpcErrors.METHOD_NOT_FOUND.code(), error.code());
        assertEquals(StdRpcErrors.METHOD_NOT_FOUND.message(), error.message());
    }

    @Test
    void resolveExecutionExceptionWrappingRpcError() {
        var inner = StdRpcErrors.INVALID_PARAMS.toException();
        var ex = new ExecutionException(inner);
        var error = RpcErrorUtils.resolveRpcError(ex);
        assertEquals(StdRpcErrors.INVALID_PARAMS.code(), error.code());
    }

    @Test
    void resolveExecutionExceptionNullCause() {
        var ex = new ExecutionException("no cause", null);
        var error = RpcErrorUtils.resolveRpcError(ex);
        assertEquals(StdRpcErrors.INTERNAL_ERROR.code(), error.code());
        assertEquals(ex.getMessage(), error.message());
    }

    @Test
    void resolveUncheckedRpcException() {
        var inner = StdRpcErrors.METHOD_NOT_FOUND.toException();
        var ex = new UncheckedRpcException(inner);
        var error = RpcErrorUtils.resolveRpcError(ex);
        assertEquals(StdRpcErrors.METHOD_NOT_FOUND.code(), error.code());
    }

    @Test
    void resolveInterruptedException() {
        var ex = new InterruptedException();
        var error = RpcErrorUtils.resolveRpcError(ex);
        assertEquals(StdRpcErrors.INTERNAL_ERROR.code(), error.code());
        assertEquals("Interrupted", error.message());
    }

    @Test
    void resolveTimeoutException() {
        var ex = new TimeoutException();
        var error = RpcErrorUtils.resolveRpcError(ex);
        assertEquals(StdRpcErrors.INTERNAL_ERROR.code(), error.code());
        assertEquals("Timeout", error.message());
    }

    @Test
    void resolveRpcErrorExceptionAsCause() {
        var inner = StdRpcErrors.INVALID_PARAMS.toException();
        var outer = new RuntimeException("wrapper", inner);
        var error = RpcErrorUtils.resolveRpcError(outer);
        assertEquals(StdRpcErrors.INVALID_PARAMS.code(), error.code());
    }

    @Test
    void resolveGenericException() {
        var ex = new RuntimeException("something went wrong");
        var error = RpcErrorUtils.resolveRpcError(ex);
        assertEquals(StdRpcErrors.INTERNAL_ERROR.code(), error.code());
        assertEquals(ex.getMessage(), error.message());
    }

    @Test
    void resolveExecutionExceptionWithRpcErrorCauseDeep() {
        var rpcEx = StdRpcErrors.METHOD_NOT_FOUND.toException();
        var unchecked = new UncheckedRpcException(rpcEx);
        var execution = new ExecutionException(unchecked);
        var error = RpcErrorUtils.resolveRpcError(execution);
        assertEquals(StdRpcErrors.METHOD_NOT_FOUND.code(), error.code());
    }

    @Test
    void resolveRpcErrorExceptionWithData() {
        var ex = StdRpcErrors.INTERNAL_ERROR.toException("custom message", "extra data");
        var error = RpcErrorUtils.resolveRpcError(ex);
        assertEquals(StdRpcErrors.INTERNAL_ERROR.code(), error.code());
        assertEquals("custom message", error.message());
    }

    @Test
    void resolveExecutionExceptionWrappingNullCause() {
        // ExecutionException wrapping RuntimeException (cause is not RpcError)
        var ex = new ExecutionException("execution failed", new RuntimeException());
        var error = RpcErrorUtils.resolveRpcError(ex);
        assertEquals(StdRpcErrors.INTERNAL_ERROR.code(), error.code());
    }

    @Test
    void resolveExecutionExceptionWrappingTimeoutException() {
        var timeout = new TimeoutException("timed out");
        var ex = new ExecutionException(timeout);
        var error = RpcErrorUtils.resolveRpcError(ex);
        assertEquals(StdRpcErrors.INTERNAL_ERROR.code(), error.code());
        assertEquals("Timeout", error.message());
    }

    @Test
    void resolveExecutionExceptionWrappingInterruptedException() {
        var interrupted = new InterruptedException("interrupted");
        var ex = new ExecutionException(interrupted);
        var error = RpcErrorUtils.resolveRpcError(ex);
        assertEquals(StdRpcErrors.INTERNAL_ERROR.code(), error.code());
        assertEquals("Interrupted", error.message());
    }

    @Test
    void resolveRpcErrorExceptionWithNullMessage() {
        var ex = StdRpcErrors.INTERNAL_ERROR.toException(null);
        var error = RpcErrorUtils.resolveRpcError(ex);
        assertEquals(StdRpcErrors.INTERNAL_ERROR.code(), error.code());
    }

    @Test
    void resolveRpcErrorWrappedInUncheckedRpcExceptionWithoutCause() {
        // UncheckedRpcException with a target exception that is not RpcErrorException
        var target = new RuntimeException("plain error");
        var ex = new UncheckedRpcException(target);
        var error = RpcErrorUtils.resolveRpcError(ex);
        assertEquals(StdRpcErrors.INTERNAL_ERROR.code(), error.code());
        assertEquals("plain error", error.message());
    }

    @Test
    void resolveRpcErrorWrappedInUncheckedRpcExceptionWithGenericCause() {
        // UncheckedRpcException wrapping a generic Exception (not RpcErrorException)
        var inner = new IllegalStateException("inner state");
        var ex = new UncheckedRpcException(inner);
        var error = RpcErrorUtils.resolveRpcError(ex);
        assertEquals(StdRpcErrors.INTERNAL_ERROR.code(), error.code());
        assertEquals("inner state", error.message());
    }
}
