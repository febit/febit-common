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

import org.tabletest.junit.TableTest;

import org.febit.common.jsonrpc2.exception.UncheckedRpcException;
import org.febit.common.jsonrpc2.protocol.StdRpcErrors;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class RpcErrorUtilsTest {

    @TableTest("""
            kind                  | code             | message
            directRpc             | METHOD_NOT_FOUND | Method not found
            execWrappingRpc       | INVALID_PARAMS   |
            execNullCause         | INTERNAL_ERROR   | no cause
            uncheckedRpc          | METHOD_NOT_FOUND |
            interrupted           | INTERNAL_ERROR   | Interrupted
            timeout               | INTERNAL_ERROR   | Timeout
            runtimeWithRpcCause   | INVALID_PARAMS   |
            genericRuntime        | INTERNAL_ERROR   | something went wrong
            execUncheckedRpc      | METHOD_NOT_FOUND |
            rpcCustomMessage      | INTERNAL_ERROR   | custom message
            execRuntimeCause      | INTERNAL_ERROR   |
            execTimeout           | INTERNAL_ERROR   | Timeout
            execInterrupted       | INTERNAL_ERROR   | Interrupted
            rpcNullMessage        | INTERNAL_ERROR   |
            uncheckedRuntime      | INTERNAL_ERROR   | plain error
            uncheckedIllegalState | INTERNAL_ERROR   | inner state
            """)
    void resolveRpcError(String kind, String code, String message) {
        var error = RpcErrorUtils.resolveRpcError(exceptionOf(kind));
        assertEquals(StdRpcErrors.valueOf(code).code(), error.code());
        if (message != null) {
            assertEquals(message, error.message());
        }
    }

    private static Throwable exceptionOf(String kind) {
        return switch (kind) {
            case "directRpc" -> StdRpcErrors.METHOD_NOT_FOUND.toException();
            case "execWrappingRpc" -> new ExecutionException(StdRpcErrors.INVALID_PARAMS.toException());
            case "execNullCause" -> new ExecutionException("no cause", null);
            case "uncheckedRpc" -> new UncheckedRpcException(StdRpcErrors.METHOD_NOT_FOUND.toException());
            case "interrupted" -> new InterruptedException();
            case "timeout" -> new TimeoutException();
            case "runtimeWithRpcCause" -> new RuntimeException("wrapper", StdRpcErrors.INVALID_PARAMS.toException());
            case "genericRuntime" -> new RuntimeException("something went wrong");
            case "execUncheckedRpc" -> new ExecutionException(new UncheckedRpcException(
                    StdRpcErrors.METHOD_NOT_FOUND.toException()));
            case "rpcCustomMessage" -> StdRpcErrors.INTERNAL_ERROR.toException("custom message", "extra data");
            case "execRuntimeCause" -> new ExecutionException("execution failed", new RuntimeException());
            case "execTimeout" -> new ExecutionException(new TimeoutException("timed out"));
            case "execInterrupted" -> new ExecutionException(new InterruptedException("interrupted"));
            case "rpcNullMessage" -> StdRpcErrors.INTERNAL_ERROR.toException(null);
            case "uncheckedRuntime" -> new UncheckedRpcException(new RuntimeException("plain error"));
            case "uncheckedIllegalState" -> new UncheckedRpcException(new IllegalStateException("inner state"));
            default -> throw new IllegalStateException("unexpected kind: " + kind);
        };
    }
}
