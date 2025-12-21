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
import org.febit.common.jsonrpc2.annotation.RpcParamsKind;
import org.febit.lang.util.ArraysUtils;
import org.febit.lang.util.Maps;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@UtilityClass
public class ParamsComposers {

    public static ParamsComposer resolve(RpcMappingMeta meta) {
        var paramsKind = meta.paramsKind();
        var paramTypes = meta.targetMethod().getParameterTypes();

        if (paramsKind == RpcParamsKind.FIRST_ARGUMENT) {
            return switch (paramTypes.length) {
                case 0 -> ParamsComposers.nil();
                case 1 -> ParamsComposers.first();
                default -> throw new IllegalStateException(
                        "ParamsKind.FIRST_ARGUMENT requires method with exactly one parameter: " + meta.method());
            };
        }

        if (paramsKind == RpcParamsKind.FLATTEN_LIST) {
            return ParamsComposers.flattenList();
        }

        if (paramsKind == RpcParamsKind.FLATTEN_OBJECT) {
            var paramNames = ArraysUtils.collect(
                    meta.targetMethod().getParameters(),
                    String[]::new, Parameter::getName
            );
            return ParamsComposers.flattenObject(paramNames);
        }
        throw new IllegalStateException("Unsupported ParamsKind: " + paramsKind + " for method: " + meta.method());
    }

    public static ParamsComposer nil() {
        return args -> null;
    }

    public static ParamsComposer first() {
        return args -> args != null && args.length > 0 ? args[0] : null;
    }

    public static ParamsComposer flattenList() {
        return args -> args == null
                ? List.of()
                : Arrays.asList(args);
    }

    public static ParamsComposer flattenObject(String[] names) {
        return args -> {
            if (args == null) {
                return null;
            }
            if (names.length == 0) {
                return Map.of();
            }
            var params = Maps.create(names.length);
            for (int i = 0; i < names.length; i++) {
                params.put(names[i], ArraysUtils.get(args, i));
            }
            return params;
        };

    }
}
