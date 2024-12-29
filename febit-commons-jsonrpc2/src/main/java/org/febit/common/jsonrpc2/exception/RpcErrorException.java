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
package org.febit.common.jsonrpc2.exception;

import jakarta.annotation.Nullable;
import lombok.Getter;
import org.febit.common.jsonrpc2.protocol.IRpcError;

@Getter
public class RpcErrorException extends RuntimeException {

    private final IRpcError<?> error;

    public RpcErrorException(IRpcError<?> error) {
        this(error, null);
    }

    public RpcErrorException(IRpcError<?> error, @Nullable Throwable cause) {
        super(formatMessage(error), cause);
        this.error = error;
    }

    private static String formatMessage(IRpcError<?> error) {
        return "[" + error.code() + "] " + error.message();
    }
}
