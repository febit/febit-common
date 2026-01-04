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

import org.febit.common.jsonrpc2.annotation.RpcMethodType;
import org.febit.common.jsonrpc2.annotation.RpcParamsKind;
import org.febit.lang.annotation.NullableArgs;
import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JavaType;

import java.lang.reflect.Method;
import java.time.Duration;

@lombok.Builder(
        builderClassName = "Builder"
)
@NullableArgs
public record RpcMappingMeta(
        @lombok.NonNull @NonNull String method,
        @lombok.NonNull @NonNull RpcMethodType type,
        @lombok.NonNull @NonNull RpcParamsKind paramsKind,
        @lombok.NonNull @NonNull JavaType resultType,
        Method targetMethod,
        boolean isFutureResult,
        boolean annotated,
        Duration timeout
) {
}
