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

import org.febit.common.jsonrpc2.exception.JsonrpcErrorException;
import org.febit.common.jsonrpc2.internal.ErrorImpl;
import org.febit.common.jsonrpc2.internal.IdImpl;
import org.febit.common.jsonrpc2.internal.Notification;
import org.febit.common.jsonrpc2.internal.Request;
import org.febit.common.jsonrpc2.internal.Response;
import org.febit.common.jsonrpc2.protocol.IRpcMessage;
import org.febit.common.jsonrpc2.protocol.SpecRpcErrors;
import org.febit.lang.util.JacksonUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.map;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.febit.common.jsonrpc2.JsonCodec.decode;
import static org.junit.jupiter.api.Assertions.*;

class JsonCodecTest {

    static Map<String, Object> encodeToMap(IRpcMessage message) {
        return Objects.requireNonNull(JacksonUtils.parseToNamedMap(
                JsonCodec.encode(message)
        ));
    }

    @Test
    void encode_happy() {
        // Notification
        assertThat(encodeToMap(new Notification("test", List.of())))
                .hasSize(3)
                .containsEntry("method", "test")
                .containsEntry("jsonrpc", "2.0")
                .containsEntry("params", List.of())
                .doesNotContainKeys(
                        "id", "result", "error"
                )
        ;

        // Request
        assertThat(encodeToMap(new Request(IdImpl.of(1), "test", List.of(1, "test"))))
                .hasSize(4)
                .containsEntry("id", 1)
                .containsEntry("method", "test")
                .containsEntry("jsonrpc", "2.0")
                .containsEntry("params", List.of(1, "test"))
                .doesNotContainKeys(
                        "result", "error"
                )
        ;

        // Response
        assertThat(encodeToMap(new Response<>(
                IdImpl.of("100"),
                "test",
                null
        )))
                .hasSize(3)
                .containsEntry("id", "100")
                .containsEntry("jsonrpc", "2.0")
                .containsEntry("result", "test")
                .doesNotContainKeys(
                        "error"
                )
        ;

        // Response with error
        assertThat(encodeToMap(new Response<>(
                IdImpl.of(1.01D),
                null,
                new ErrorImpl<>(-32601, "Method not found", null)
        )))
                .hasSize(3)
                .containsEntry("id", 1.01D)
                .containsEntry("jsonrpc", "2.0")
                .containsKey("error")
                .doesNotContainKeys(
                        "result"
                )
                .extracting("error")
                .asInstanceOf(map(String.class, Object.class))
                .containsEntry("code", -32601)
                .containsEntry("message", "Method not found")
                .doesNotContainKey("data");
    }

    @Test
    void decode_happy() {

        // Notification
        assertThat(decode("""
                {
                    "jsonrpc": "2.0",
                    "method": "test",
                    "params": []
                }
                """)).asInstanceOf(type(Notification.class))
                .returns("test", Notification::method)
                .returns(List.of(), Notification::params);

        // Request
        assertThat(decode("""
                {
                    "jsonrpc": "2.0",
                    "id": 1,
                    "method": "test",
                    "params": [1, "test"]
                }
                """)).asInstanceOf(type(Request.class))
                .returns(IdImpl.of(1), Request::id)
                .returns("test", Request::method)
                .returns(List.of(1, "test"), Request::params);

        // Response
        assertThat(decode("""
                {
                    "jsonrpc": "2.0",
                    "id": "100",
                    "result": "test"
                }
                """)).asInstanceOf(type(Response.class))
                .returns(IdImpl.of("100"), Response::id)
                .returns("test", Response::result);

        // Response with error

        assertThat(decode("""
                {
                    "jsonrpc": "2.0",
                    "id": 1.01,
                    "error": {
                        "code": -32601,
                        "message": "Method not found"
                    }
                }
                """)).asInstanceOf(type(Response.class))
                .returns(IdImpl.of(1.01D), Response::id)
                .returns(null, Response::result)
                .extracting(Response::error)
                .isNotNull()
                .returns(-32601, ErrorImpl::code)
                .returns("Method not found", ErrorImpl::message)
                .returns(null, ErrorImpl::data);
    }

    @Test
    void decode_bad() {

        Stream.of(
                null, "", "null", "[]", "1", "false", "\"\"", "invalid json"
        ).forEach(json -> {
            var ex = assertThrows(JsonrpcErrorException.class, () -> {
                decode(json);
            });
            assertEquals(SpecRpcErrors.PARSE_ERROR.code(), ex.getError().code());
        });

        // invalid version
        var ex = assertThrows(JsonrpcErrorException.class, () -> {
            decode("""
                    {
                        "jsonrpc": "1.0",
                        "id": 1,
                        "method": "test",
                        "params": []
                    }
                    """);
        });
        assertEquals(SpecRpcErrors.INVALID_REQUEST.code(), ex.getError().code());

        // No id and method
        ex = assertThrows(JsonrpcErrorException.class, () -> {
            decode("""
                    {
                        "jsonrpc": "2.0",
                        "params": []
                    }
                    """);
        });
        assertEquals(SpecRpcErrors.INVALID_REQUEST.code(), ex.getError().code());
    }

}
