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

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.febit.common.jsonrpc2.exception.JsonrpcErrorException;
import org.febit.common.jsonrpc2.internal.Notification;
import org.febit.common.jsonrpc2.internal.Request;
import org.febit.common.jsonrpc2.internal.Response;
import org.febit.common.jsonrpc2.protocol.IRpcMessage;
import org.febit.common.jsonrpc2.protocol.SpecRpcErrors;
import org.febit.lang.util.JacksonUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class JsonCodec {

    public static String encode(IRpcMessage message) {
        return JacksonUtils.jsonify(message);
    }

    /**
     * Decode a json string to RpcMessage.
     *
     * @param text json string
     * @return RpcMessage
     * @throws JsonrpcErrorException when not a valid message
     */
    public static IRpcMessage decode(@Nullable String text) {
        if (StringUtils.isEmpty(text)) {
            throw SpecRpcErrors.PARSE_ERROR
                    .toException("message is empty");
        }

        Map<String, Object> json;
        try {
            json = JacksonUtils.parseToNamedMap(text);
        } catch (Exception e) {
            throw SpecRpcErrors.PARSE_ERROR
                    .toException("invalid json", e);
        }
        if (json == null) {
            throw SpecRpcErrors.PARSE_ERROR
                    .toException("invalid json");
        }
        if (!Jsonrpc2.VERSION.equals(json.get("jsonrpc"))) {
            throw SpecRpcErrors.INVALID_REQUEST
                    .toException("invalid message version");
        }

        var id = json.get("id");
        var method = json.get("method");

        if (id == null && method == null) {
            throw SpecRpcErrors.INVALID_REQUEST
                    .toException("invalid request or notification or response");
        }
        if (method == null) {
            return to(json, Response.class);
        }

        json.computeIfAbsent("params", k -> List.of());
        if (id == null) {
            return to(json, Notification.class);
        }
        return to(json, Request.class);
    }

    private static <T> T to(Map<String, Object> json, Class<T> type) {
        var result = JacksonUtils.to(json, type);
        Objects.requireNonNull(result, "Invalid message");
        return result;
    }
}
