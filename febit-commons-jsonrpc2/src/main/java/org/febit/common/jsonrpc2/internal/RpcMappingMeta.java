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

import com.fasterxml.jackson.databind.JavaType;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import org.febit.common.jsonrpc2.RpcMapping;
import org.febit.lang.annotation.NullableArgs;

import java.lang.reflect.Method;
import java.time.Duration;

@lombok.Builder(
        builderClassName = "Builder"
)
@NullableArgs
public record RpcMappingMeta(
        @NonNull @Nonnull String method,
        @NonNull @Nonnull RpcMapping.Type type,
        @NonNull @Nonnull RpcMapping.ParamsKind paramsKind,
        @NonNull @Nonnull JavaType resultType,
        Method targetMethod,
        boolean isFutureResult,
        boolean annotated,
        Duration timeout
) {
}
