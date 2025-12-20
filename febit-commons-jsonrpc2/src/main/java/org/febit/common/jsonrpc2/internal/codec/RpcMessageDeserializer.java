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
package org.febit.common.jsonrpc2.internal.codec;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import jakarta.annotation.Nullable;
import org.febit.common.jsonrpc2.Jsonrpc2;
import org.febit.common.jsonrpc2.internal.protocol.Notification;
import org.febit.common.jsonrpc2.internal.protocol.Request;
import org.febit.common.jsonrpc2.internal.protocol.Response;
import org.febit.common.jsonrpc2.protocol.IRpcMessage;

import java.io.IOException;

public class RpcMessageDeserializer extends StdDeserializer<IRpcMessage> {

    private static final String ID = "id";
    private static final String METHOD = "method";

    public RpcMessageDeserializer() {
        super(IRpcMessage.class);
    }

    @Nullable
    @Override
    public IRpcMessage deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        var tree = parser.readValueAsTree();

        if (!(tree instanceof JsonNode node)) {
            throw new JsonMappingException(parser,
                    "Invalid JSON-RPC message: not a JSON object.");
        }

        if (node.isNull()) {
            return null;
        }

        if (!node.isObject()) {
            throw new JsonMappingException(parser,
                    "Invalid JSON-RPC message: not a JSON object.");
        }

        var versionNode = node.get("jsonrpc");
        if (!nonNullNode(versionNode)) {
            throw new JsonMappingException(parser,
                    "Invalid JSON-RPC message: missing 'jsonrpc' property.");
        }
        var version = versionNode.asText();
        if (!Jsonrpc2.VERSION.equals(version)) {
            throw new JsonMappingException(parser,
                    "Invalid JSON-RPC message: unsupported 'jsonrpc' version"
                            + ", only " + Jsonrpc2.VERSION + " is supported.");
        }

        var hasId = nonNullNode(node.get(ID));
        var hasMethod = nonNullNode(node.get(METHOD));

        Class<? extends IRpcMessage> targetType;
        targetType = switch ((hasId ? 1 : 0) | (hasMethod ? 2 : 0)) {
            case 1 -> Response.class;
            case 2 -> Notification.class;
            case 3 -> Request.class;
            default -> throw new JsonMappingException(
                    parser,
                    "Invalid JSON-RPC message: missing both 'id' and 'method' properties."
            );
        };
        return context.readTreeAsValue(node, targetType);
    }

    private static boolean nonNullNode(@Nullable JsonNode node) {
        return node != null && !node.isNull();
    }
}
