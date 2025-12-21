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
import lombok.experimental.UtilityClass;
import org.febit.common.jsonrpc2.JsonCodec;
import org.febit.common.jsonrpc2.annotation.RpcMapping;
import org.febit.common.jsonrpc2.annotation.RpcMethodType;
import org.febit.common.jsonrpc2.annotation.RpcParamsKind;
import org.febit.lang.util.TypeParameters;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.Future;

@UtilityClass
public class RpcMappings {

    private static final String SPLITTER = "/";

    public static boolean annotated(Method method) {
        return AnnotatedElementUtils.findMergedAnnotation(method, RpcMapping.class) != null;
    }

    private static JavaType resolveResultType(Method method) {
        var rawType = method.getGenericReturnType();
        if (!Future.class.isAssignableFrom(method.getReturnType())) {
            return JsonCodec.resolveType(rawType);
        }
        var inner = TypeParameters.forType(rawType)
                .resolve(Future.class, 0).get();
        return JsonCodec.resolveType(inner == null ? Object.class : inner);
    }

    public static RpcMappingMeta resolve(Method method) {
        var baseAnno = AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), RpcMapping.class);
        var basePath = baseAnno == null ? "" : baseAnno.value();

        var isFutureResult = Future.class.isAssignableFrom(method.getReturnType());
        var resultType = resolveResultType(method);

        var builder = RpcMappingMeta.builder()
                .targetMethod(method)
                .resultType(resultType)
                .isFutureResult(isFutureResult);

        if (baseAnno != null && baseAnno.timeout() > 0) {
            builder.timeout(Duration.ofMillis(baseAnno.timeout()));
        }

        var anno = AnnotatedElementUtils.findMergedAnnotation(method, RpcMapping.class);
        if (anno == null) {
            return builder
                    .annotated(false)
                    .method(basePath.isEmpty() ? method.getName()
                            : basePath + SPLITTER + method.getName()
                    )
                    .type(RpcMethodType.REQUEST)
                    .paramsKind(baseAnno == null
                            ? RpcParamsKind.FIRST_ARGUMENT
                            : baseAnno.paramsKind())
                    .build();
        }

        builder.annotated(true);
        builder.type(anno.type());
        builder.paramsKind(anno.paramsKind());

        if (anno.timeout() < 0) {
            builder.timeout(null);
        } else if (anno.timeout() > 0) {
            builder.timeout(Duration.ofMillis(anno.timeout()));
        }

        var path = anno.value().isEmpty() ? method.getName() : anno.value();
        builder.method(basePath.isEmpty() ? path
                : basePath + SPLITTER + path
        );
        return builder.build();
    }
}
