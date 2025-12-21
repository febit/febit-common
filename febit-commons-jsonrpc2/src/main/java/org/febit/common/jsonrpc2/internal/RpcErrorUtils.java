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

import lombok.experimental.UtilityClass;
import org.febit.common.jsonrpc2.exception.RpcErrorException;
import org.febit.common.jsonrpc2.exception.UncheckedRpcException;
import org.febit.common.jsonrpc2.protocol.IRpcError;
import org.febit.common.jsonrpc2.protocol.StdRpcErrors;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@UtilityClass
public class RpcErrorUtils {

    public static IRpcError<?> resolveRpcError(Throwable original) {
        // Unwrap ExecutionException
        if (original instanceof ExecutionException) {
            if (original.getCause() == null) {
                return StdRpcErrors.INTERNAL_ERROR.toError(original.getMessage());
            }
            original = original.getCause();
        }
        if (original instanceof UncheckedRpcException ex) {
            original = ex.getTargetException();
        }

        if (original instanceof RpcErrorException ex) {
            return ex.getError();
        }
        if (original instanceof InterruptedException) {
            return StdRpcErrors.INTERNAL_ERROR.toError("Interrupted");
        }
        if (original instanceof TimeoutException) {
            return StdRpcErrors.INTERNAL_ERROR.toError("Timeout");
        }
        if (original.getCause() instanceof RpcErrorException ex) {
            return ex.getError();
        }
        return StdRpcErrors.INTERNAL_ERROR.toError(original.getMessage());
    }
}
