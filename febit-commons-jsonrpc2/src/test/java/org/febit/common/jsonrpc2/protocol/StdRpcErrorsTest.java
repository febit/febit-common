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
package org.febit.common.jsonrpc2.protocol;

import org.tabletest.junit.TableTest;

import org.febit.common.jsonrpc2.exception.RpcErrorException;

import static org.junit.jupiter.api.Assertions.*;

class StdRpcErrorsTest {

    @TableTest("""
            error            | code   | message          | descriptionContains
            PARSE_ERROR      | -32700 | Parse error      | Invalid JSON
            INVALID_REQUEST  | -32600 | Invalid Request  | valid Request
            METHOD_NOT_FOUND | -32601 | Method not found | not exist
            INVALID_PARAMS   | -32602 | Invalid params   | Invalid method parameter
            INTERNAL_ERROR   | -32603 | Internal error   | Internal JSON-RPC error
            """)
    void definesCodeMessageAndDescription(StdRpcErrors error, int code, String message, String descriptionContains) {
        assertEquals(code, error.code());
        assertEquals(message, error.message());
        assertTrue(error.description().contains(descriptionContains));
    }

    @TableTest("""
            error           | variant | messageArg | expectedCode | expectedMessage | expectedData
            PARSE_ERROR     | default |            | -32700       | Parse error     |
            INVALID_REQUEST | message | custom msg | -32600       | custom msg      |
            INTERNAL_ERROR  | data    | internal   | -32603       | internal        | data-value
            """)
    void toError(StdRpcErrors error, String variant, String messageArg, int expectedCode, String expectedMessage, String expectedData) {
        Object data = "data".equals(variant) ? expectedData : null;
        IRpcError<?> result;
        if ("default".equals(variant)) {
            result = error.toError();
        } else if ("message".equals(variant)) {
            result = error.toError(messageArg);
        } else {
            result = error.toError(messageArg, data);
        }
        assertEquals(expectedCode, result.code());
        assertEquals(expectedMessage, result.message());
        assertEquals(expectedData, result.data());
    }

    @TableTest("""
            error            | variant | messageArg | expectedCode | expectedMessage | expectedData | expectCause
            PARSE_ERROR      | default |            | -32700       | Parse error     |              | false
            INVALID_REQUEST  | message | custom msg | -32600       | custom msg      |              | false
            METHOD_NOT_FOUND | message | not found  | -32601       | not found       |              | false
            INTERNAL_ERROR   | cause   | failed     | -32603       | failed          |              | true
            INVALID_PARAMS   | data    | bad        | -32602       | bad             | detail       | false
            """)
    void toException(StdRpcErrors error, String variant, String messageArg, int expectedCode, String expectedMessage, String expectedData, boolean expectCause) {
        Exception cause = "cause".equals(variant) ? new RuntimeException("root cause") : null;
        Object data = "data".equals(variant) ? expectedData : null;
        RpcErrorException ex;
        if ("default".equals(variant)) {
            ex = error.toException();
        } else if ("message".equals(variant)) {
            ex = error.toException(messageArg);
        } else if ("cause".equals(variant)) {
            ex = error.toException(messageArg, cause);
        } else {
            ex = error.toException(messageArg, data);
        }
        assertEquals(expectedCode, ex.getError().code());
        assertEquals(expectedMessage, ex.getError().message());
        assertEquals(expectedData, ex.getError().data());
        if (expectCause) {
            assertEquals(cause, ex.getCause());
        } else {
            assertNull(ex.getCause());
        }
    }
}
