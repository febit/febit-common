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
import org.febit.common.jsonrpc2.protocol.StdRpcErrors;
import org.febit.lang.util.JacksonUtils;
import org.febit.lang.util.Lists;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

@UtilityClass
public class ArgumentsResolvers {

    public static ArgumentsResolver resolve(RpcMappingMeta meta) {
        var paramTypes = JsonCodec.resolveParameterTypes(meta.targetMethod());
        if (paramTypes.isEmpty()) {
            return ArgumentsResolvers.empty();
        }

        return switch (meta.paramsKind()) {
            case FIRST_ARGUMENT -> {
                if (paramTypes.size() != 1) {
                    throw new IllegalStateException("ParamsKind.FIRST_ARGUMENT requires exactly one parameter.");
                }
                yield ArgumentsResolvers.first(paramTypes.get(0));
            }
            case FLATTEN_LIST -> ArgumentsResolvers.flattenList(paramTypes);
            case FLATTEN_OBJECT -> {
                var names = Lists.collect(
                        meta.targetMethod().getParameters(),
                        Parameter::getName
                );
                yield ArgumentsResolvers.flattenObject(names, paramTypes);
            }
        };
    }

    public static ArgumentsResolver empty() {
        return params -> new Object[0];
    }

    public static ArgumentsResolver flattenList(List<JavaType> paramTypes) {
        return params -> {
            var args = new Object[paramTypes.size()];
            if (params == null) {
                return args;
            }

            List<Object> list;
            try {
                list = JacksonUtils.toList(params);
            } catch (Exception e) {
                throw StdRpcErrors.INVALID_PARAMS.toException("Invalid params: expected an array/list"
                        + ", but got: " + params.getClass(), e);
            }
            if (list == null) {
                return args;
            }
            var limit = Math.min(args.length, list.size());
            for (int i = 0; i < limit; i++) {
                args[i] = JsonCodec.convert(list.get(i), paramTypes.get(i));
            }
            return args;
        };
    }

    public static ArgumentsResolver flattenObject(List<String> names, List<JavaType> paramTypes) {
        return params -> {
            var args = new Object[paramTypes.size()];
            if (params == null) {
                return args;
            }

            Map<String, Object> map;
            try {
                map = JacksonUtils.toNamedMap(params);
            } catch (Exception e) {
                throw StdRpcErrors.INVALID_PARAMS.toException("Invalid params: expected an object/map"
                        + ", but got: " + params.getClass(), e);
            }
            if (map == null) {
                return args;
            }
            var limit = Math.min(names.size(), paramTypes.size());
            for (int i = 0; i < limit; i++) {
                var name = names.get(i);
                var rawValue = map.get(name);
                args[i] = JsonCodec.convert(rawValue, paramTypes.get(i));
            }
            return args;
        };

    }

    public static ArgumentsResolver first(JavaType paramType) {
        return params -> {
            if (params == null) {
                return new Object[]{null};
            }
            return new Object[]{
                    JsonCodec.convert(params, paramType)
            };
        };
    }

}
