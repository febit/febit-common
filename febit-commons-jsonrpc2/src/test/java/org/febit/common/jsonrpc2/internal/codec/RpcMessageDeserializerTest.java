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

import org.assertj.core.api.Assertions;
import org.febit.common.jsonrpc2.internal.protocol.Request;
import org.febit.common.jsonrpc2.protocol.IRpcMessage;
import org.febit.common.jsonrpc2.protocol.Id;
import org.febit.lang.jackson.JacksonUtils;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DatabindException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RpcMessageDeserializerTest {

    static IRpcMessage parse(String json) {
        return JacksonUtils.parse(json, IRpcMessage.class);
    }

    @Test
    void nul() {
        assertNull(parse("null"));
    }

    @Test
    void invalidNotObject() {
        Assertions.assertThatThrownBy(() -> parse("123"))
                .isInstanceOf(DatabindException.class)
                .hasMessageContaining("not a JSON object");
    }

    @Test
    void invalidMissingVersion() {
        Assertions.assertThatThrownBy(() -> parse("""
                        {
                          "id": 1,
                          "method": "subtract",
                          "params": [42, 23]
                        }
                        """))
                .isInstanceOf(DatabindException.class)
                .hasMessageContaining("missing 'jsonrpc' property");
    }

    @Test
    void invalidUnsupportedVersion() {
        Assertions.assertThatThrownBy(() -> parse("""
                        {
                          "jsonrpc": "1.0",
                          "id": 1,
                          "method": "subtract",
                          "params": [42, 23]
                        }
                        """))
                .isInstanceOf(DatabindException.class)
                .hasMessageContaining("unsupported 'jsonrpc' version");
    }

    @Test
    void request() {
        var msg = parse("""
                {
                  "jsonrpc": "2.0",
                  "id": 1,
                  "method": "subtract",
                  "params": [42, 23]
                }
                """);

        assertInstanceOf(Request.class, msg);
        assertEquals(new Request(
                Id.of(1),
                "subtract",
                List.of(42, 23)
        ), msg);
    }

    @Test
    void requestNoParams() {
        var msg = parse("""
                {
                  "jsonrpc": "2.0",
                  "id": "1",
                  "method": "subtract"
                }
                """);

        assertInstanceOf(Request.class, msg);
        assertEquals(new Request(
                Id.of("1"),
                "subtract",
                null
        ), msg);
    }

    @Test
    void invalidMissingIdAndMethod() {
        Assertions.assertThatThrownBy(() -> parse("""
                        {
                          "jsonrpc": "2.0"
                        }
                        """))
                .isInstanceOf(DatabindException.class)
                .hasMessageContaining("missing both 'id' and 'method' properties");
    }

    @Test
    void invalidIdBoolean() {
        Assertions.assertThatThrownBy(() -> parse("""
                        {
                          "jsonrpc": "2.0",
                          "id": true,
                          "method": "test"
                        }
                        """))
                .isInstanceOf(DatabindException.class)
                .hasMessageContaining("rpc message id must be string or number");
    }

    @Test
    void invalidIdArray() {
        Assertions.assertThatThrownBy(() -> parse("""
                        {
                          "jsonrpc": "2.0",
                          "id": [1, 2],
                          "method": "test"
                        }
                        """))
                .isInstanceOf(DatabindException.class)
                .hasMessageContaining("rpc message id must be string or number");
    }

    @Test
    void invalidIdObject() {
        Assertions.assertThatThrownBy(() -> parse("""
                        {
                          "jsonrpc": "2.0",
                          "id": {"nested": true},
                          "method": "test"
                        }
                        """))
                .isInstanceOf(DatabindException.class)
                .hasMessageContaining("rpc message id must be string or number");
    }

    @Test
    void notification() {
        var msg = parse("""
                {
                  "jsonrpc": "2.0",
                  "method": "update",
                  "params": ["data"]
                }
                """);

        assertInstanceOf(org.febit.common.jsonrpc2.internal.protocol.Notification.class, msg);
        var notif = (org.febit.common.jsonrpc2.internal.protocol.Notification) msg;
        assertEquals("update", notif.method());
        assertEquals(List.of("data"), notif.params());
    }

    @Test
    void responseWithResult() {
        var msg = parse("""
                {
                  "jsonrpc": "2.0",
                  "id": 1,
                  "result": 42
                }
                """);

        assertInstanceOf(org.febit.common.jsonrpc2.internal.protocol.Response.class, msg);
        var resp = (org.febit.common.jsonrpc2.internal.protocol.Response<?>) msg;
        assertEquals(Id.of(1), resp.id());
        assertEquals(42, resp.result());
        assertNull(resp.error());
    }

    @Test
    void responseWithError() {
        var msg = parse("""
                {
                  "jsonrpc": "2.0",
                  "id": 1,
                  "error": {
                    "code": -32600,
                    "message": "Invalid Request"
                  }
                }
                """);

        assertInstanceOf(org.febit.common.jsonrpc2.internal.protocol.Response.class, msg);
        var resp = (org.febit.common.jsonrpc2.internal.protocol.Response<?>) msg;
        assertEquals(Id.of(1), resp.id());
        assertNull(resp.result());
        assertNotNull(resp.error());
        assertEquals(-32600, resp.error().code());
        assertEquals("Invalid Request", resp.error().message());
    }

    @Test
    void responseWithoutIdIsInvalid() {
        Assertions.assertThatThrownBy(() -> parse("""
                        {
                          "jsonrpc": "2.0",
                          "result": 42
                        }
                        """))
                .isInstanceOf(DatabindException.class)
                .hasMessageContaining("missing both 'id' and 'method' properties");
    }

}
