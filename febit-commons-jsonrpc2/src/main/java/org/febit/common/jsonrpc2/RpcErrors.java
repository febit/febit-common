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

import jakarta.annotation.Nullable;
import org.febit.common.jsonrpc2.exception.JsonrpcErrorException;
import org.febit.common.jsonrpc2.internal.ErrorImpl;
import org.febit.common.jsonrpc2.protocol.IRpcError;

public interface RpcErrors {

    int code();

    String message();

    String description();

    default <T> IRpcError<T> toError() {
        return new ErrorImpl<>(code(), message(), null);
    }

    default <T> IRpcError<T> toError(String message) {
        return new ErrorImpl<>(code(), message(), null);
    }

    default <T> IRpcError<T> toError(String message, T data) {
        return new ErrorImpl<>(code(), message, data);
    }

    default JsonrpcErrorException toException() {
        return new JsonrpcErrorException(toError());
    }

    default JsonrpcErrorException toException(String message) {
        return toException(message, (Exception) null);
    }

    default JsonrpcErrorException toException(String message, @Nullable Exception cause) {
        return new JsonrpcErrorException(toError(message), cause);
    }

    default JsonrpcErrorException toException(String message, Object data) {
        return new JsonrpcErrorException(toError(message, data));
    }
}
