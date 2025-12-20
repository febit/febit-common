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

import org.febit.common.jsonrpc2.internal.protocol.Request;
import org.febit.common.jsonrpc2.protocol.IRpcMessage;
import org.febit.common.jsonrpc2.protocol.Id;
import org.febit.lang.util.JacksonUtils;
import org.junit.jupiter.api.Test;

import java.io.UncheckedIOException;
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
        var exception = assertThrows(UncheckedIOException.class, () -> parse("123"));
        assertTrue(exception.getCause().getMessage().contains("not a JSON object"));
    }

    @Test
    void invalidMissingVersion() {
        var exception = assertThrows(UncheckedIOException.class, () -> parse("""
                {
                  "id": 1,
                  "method": "subtract",
                  "params": [42, 23]
                }
                """));
        assertTrue(exception.getCause().getMessage().contains("missing 'jsonrpc' property"));
    }

    @Test
    void invalidUnsupportedVersion() {
        var exception = assertThrows(UncheckedIOException.class, () -> parse("""
                {
                  "jsonrpc": "1.0",
                  "id": 1,
                  "method": "subtract",
                  "params": [42, 23]
                }
                """));
        assertTrue(exception.getCause().getMessage().contains("unsupported 'jsonrpc' version"));
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
        var exception = assertThrows(UncheckedIOException.class, () -> parse("""
                {
                  "jsonrpc": "2.0"
                }
                """));
        assertTrue(exception.getCause().getMessage().contains("missing both 'id' and 'method' properties"));
    }

}
