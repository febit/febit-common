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

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.febit.common.jsonrpc2.exception.RpcErrorException;
import org.febit.common.jsonrpc2.protocol.IRpcMessage;
import org.febit.common.jsonrpc2.protocol.StdRpcErrors;
import org.febit.lang.util.JacksonUtils;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JavaType;

import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@UtilityClass
public class JsonCodec {

    public static JavaType resolveType(Type type) {
        return JacksonUtils.TYPES.constructType(type);
    }

    public static List<JavaType> resolveParameterTypes(Method method) {
        return Stream.of(method.getGenericParameterTypes())
                .map(JsonCodec::resolveType)
                .toList();
    }

    @Nullable
    public static <T> T convert(@Nullable Object result, JavaType resultType) {
        if (result == null) {
            return null;
        }
        try {
            return JacksonUtils.to(result, resultType);
        } catch (Exception e) {
            throw StdRpcErrors.INVALID_PARAMS.toException("Invalid params: " + e.getCause(), e);
        }
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
        try {
            var result = JacksonUtils.parse(text, IRpcMessage.class);
            if (result == null) {
                throw StdRpcErrors.PARSE_ERROR.toException("invalid message: null");
            }
            return result;
        } catch (RpcErrorException e) {
            throw e;
        } catch (JacksonException e) {
            throw StdRpcErrors.PARSE_ERROR
                    .toException("invalid message: " + e.getMessage(), e);
        } catch (Exception e) {
            throw StdRpcErrors.INTERNAL_ERROR
                    .toException("cannot parse message: " + e.getMessage(), e);
        }
    }

    /**
     * Decode a map to RpcMessage.
     *
     * @param raw raw map
     * @return RpcMessage
     * @throws RpcErrorException when not a valid message
     */
    public static IRpcMessage decode(@Nullable Map<String, Object> raw) {
        if (raw == null) {
            throw StdRpcErrors.PARSE_ERROR
                    .toException("invalid message, null");
        }
        try {
            var result = JacksonUtils.to(raw, IRpcMessage.class);
            Objects.requireNonNull(result, "Invalid message");
            return result;
        } catch (RpcErrorException e) {
            throw e;
        } catch (UncheckedIOException e) {
            throw StdRpcErrors.PARSE_ERROR
                    .toException("invalid message: " + e.getCause().getMessage(), e);
        } catch (Exception e) {
            throw StdRpcErrors.PARSE_ERROR
                    .toException("invalid message: " + e.getMessage(), e);
        }
    }

}
