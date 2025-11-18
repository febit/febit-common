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

import com.fasterxml.jackson.databind.JavaType;
import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.febit.common.jsonrpc2.exception.RpcErrorException;
import org.febit.common.jsonrpc2.internal.protocol.Notification;
import org.febit.common.jsonrpc2.internal.protocol.Request;
import org.febit.common.jsonrpc2.internal.protocol.Response;
import org.febit.common.jsonrpc2.protocol.IRpcMessage;
import org.febit.common.jsonrpc2.protocol.StdRpcErrors;
import org.febit.lang.util.JacksonUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@UtilityClass
public class JsonCodec {

    private static final String PROP_ID = "id";
    private static final String PROP_METHOD = "method";
    private static final String PROP_PARAMS = "params";

    public static JavaType resolveType(Type type) {
        return JacksonUtils.TYPE_FACTORY.constructType(type);
    }

    public static JavaType[] resolveParameterTypes(Method method) {
        return Stream.of(method.getGenericParameterTypes())
                .map(JsonCodec::resolveType)
                .toArray(JavaType[]::new);
    }

    @Nullable
    public static <T> T convert(@Nullable Object result, JavaType resultType) {
        if (result == null) {
            return null;
        }
        return JacksonUtils.to(result, resultType);
    }

    public static Object[] convertParameters(List<Object> rawParams, JavaType[] paramTypes) {
        var params = new Object[paramTypes.length];
        try {
            for (int i = 0; i < params.length && i < rawParams.size(); i++) {
                params[i] = JsonCodec.convert(rawParams.get(i), paramTypes[i]);
            }
        } catch (Exception e) {
            throw StdRpcErrors.INVALID_PARAMS.toException("Invalid params: " + e.getCause(), e);
        }
        return params;
    }

    public static String encode(IRpcMessage message) {
        return JacksonUtils.jsonify(message);
    }

    /**
     * Decode a json string to RpcMessage.
     *
     * @param text json string
     * @return RpcMessage
     * @throws RpcErrorException when not a valid message
     */
    public static IRpcMessage decode(@Nullable String text) {
        if (StringUtils.isEmpty(text)) {
            throw StdRpcErrors.PARSE_ERROR
                    .toException("message is empty");
        }

        Map<String, Object> json;
        try {
            json = JacksonUtils.parseToNamedMap(text);
        } catch (Exception e) {
            throw StdRpcErrors.PARSE_ERROR
                    .toException("invalid json", e);
        }
        if (json == null) {
            throw StdRpcErrors.PARSE_ERROR
                    .toException("invalid json");
        }
        if (!Jsonrpc2.VERSION.equals(json.get("jsonrpc"))) {
            throw StdRpcErrors.INVALID_REQUEST
                    .toException("invalid message version");
        }

        var id = json.get(PROP_ID);
        var method = json.get(PROP_METHOD);

        if (id == null && method == null) {
            throw StdRpcErrors.INVALID_REQUEST
                    .toException("invalid request or notification or response");
        }
        if (method == null) {
            return convertToMessage(json, Response.class);
        }

        var params = json.get(PROP_PARAMS);
        if (params == null) {
            json.put(PROP_PARAMS, List.of());
        } else if (!(params instanceof List)) {
            json.put(PROP_PARAMS, List.of(
                    params
            ));
        }

        if (id == null) {
            return convertToMessage(json, Notification.class);
        }
        return convertToMessage(json, Request.class);
    }

    private static <T> T convertToMessage(Map<String, Object> json, Class<T> type) {
        var result = JacksonUtils.to(json, type);
        Objects.requireNonNull(result, "Invalid message");
        return result;
    }
}
