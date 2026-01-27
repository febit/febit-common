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
package org.febit.common.rest.client.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.febit.common.rest.client.service.annotation.RequestParamForm;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.annotation.JsonNaming;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.febit.lang.util.JacksonUtils.TYPES;

/**
 * Resolves method arguments annotated with {@link RequestParamForm}
 * into request parameters with optional prefixing and nested structure support.
 * <p>
 * For example, given an argument annotated with {@code @RequestParamForm(prefix = "user")}
 * and a value of {@code { "name": "Alice", "age": 30 }},
 * this resolver will add the following request parameters:
 * <ul>
 *     <li>{@code user[name]} = {@code "Alice"}</li>
 *     <li>{@code user[age]} = {@code "30"}</li>
 * </ul>
 * It also supports nested maps and lists, converting them into appropriately indexed parameter names.
 * <p>
 * This resolver uses a {@link JsonMapper} to convert the argument object into a map of named values.
 * This allows it to respect any Jackson annotations present on the argument class,
 * for example:
 * {@link JsonProperty}, {@link JsonIgnore} and {@link JsonNaming}.
 * <p>
 *
 * @see RequestParamForm
 */
@RequiredArgsConstructor(staticName = "create")
public class RequestParamFormArgumentResolver implements HttpServiceArgumentResolver {

    static final JavaType TYPE_MAP_NAMED = TYPES.constructMapType(
            LinkedHashMap.class, String.class, Object.class
    );

    private final JsonMapper jsonMapper;

    @Override
    public boolean resolve(
            @Nullable Object argument,
            MethodParameter parameter,
            HttpRequestValues.Builder sink
    ) {
        var anno = parameter.getParameterAnnotation(RequestParamForm.class);
        if (anno == null) {
            return false;
        }
        if (argument == null) {
            return true;
        }

        var map = toNamedMap(argument);
        append(sink, anno.prefix(), map);
        return true;
    }

    private Map<String, Object> toNamedMap(@Nullable Object src) {
        if (src == null) {
            return Map.of();
        }
        return jsonMapper.convertValue(src, TYPE_MAP_NAMED);
    }

    private String concatPrefix(String prefix, String sub) {
        if (prefix.isEmpty()) {
            return sub;
        }
        return prefix + '[' + sub + ']';
    }

    private void append(HttpRequestValues.Builder sink, String prefix, Map<String, Object> map) {
        for (var entry : map.entrySet()) {
            var key = concatPrefix(prefix, entry.getKey());
            append(sink, key, entry.getValue());
        }
    }

    private void append(HttpRequestValues.Builder sink, String prefix, @Nullable Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                append(sink, prefix + '[' + i + ']', list.get(i));
            }
            return;
        }
        if (value instanceof Map<?, ?> map) {
            append(sink, prefix, toNamedMap(map));
            return;
        }
        var stringify = String.valueOf(value);
        sink.addRequestParameter(prefix, stringify);
    }

}

