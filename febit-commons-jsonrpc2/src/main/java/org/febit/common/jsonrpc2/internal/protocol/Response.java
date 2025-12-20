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

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.febit.common.jsonrpc2.internal.codec.IdDeserializer;
import org.febit.common.jsonrpc2.protocol.IRpcError;
import org.febit.common.jsonrpc2.protocol.IRpcResponse;
import org.febit.common.jsonrpc2.protocol.Id;

import java.util.Objects;

@JsonDeserialize(using = JsonDeserializer.None.class)
public record Response<T>(
        @Nonnull
        @JsonDeserialize(using = IdDeserializer.class)
        Id id,

        @Nullable
        T result,

        @Nullable
        @JsonDeserialize(as = ErrorImpl.class)
        IRpcError<?> error
) implements IRpcResponse<T> {

    public static <T> Response<T> ok(Id id, @Nullable T result) {
        return new Response<>(id, result, null);
    }

    public static <T> Response<T> failed(Id id, @Nonnull IRpcError<?> error) {
        Objects.requireNonNull(error, "rpc error must not be null");
        return new Response<>(id, null, error);
    }
}
