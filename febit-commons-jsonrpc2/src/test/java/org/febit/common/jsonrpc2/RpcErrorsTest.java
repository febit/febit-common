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

import org.tabletest.junit.TableTest;

import org.febit.common.jsonrpc2.exception.RpcErrorException;
import org.febit.common.jsonrpc2.protocol.IRpcError;

import static org.junit.jupiter.api.Assertions.*;

class RpcErrorsTest {

    /**
     * A custom RpcErrors implementation for testing interface default methods.
     */
    enum CustomErrors implements RpcErrors {
        CUSTOM_1(100, "Custom error one", "Description one"),
        CUSTOM_2(200, "Custom error two", "Description two"),
        ;

        private final int code;
        private final String message;
        private final String description;

        CustomErrors(int code, String message, String description) {
            this.code = code;
            this.message = message;
            this.description = description;
        }

        @Override
        public int code() {
            return code;
        }

        @Override
        public String message() {
            return message;
        }

        @Override
        public String description() {
            return description;
        }
    }

    @TableTest("""
            error    | code | message          | description
            CUSTOM_1 | 100  | Custom error one | Description one
            CUSTOM_2 | 200  | Custom error two | Description two
            """)
    void customErrorsValues(CustomErrors error, int code, String message, String description) {
        assertEquals(code, error.code());
        assertEquals(message, error.message());
        assertEquals(description, error.description());
    }

    @TableTest("""
            error    | variant       | messageArg         | expectedCode | expectedMessage    | expectedData
            CUSTOM_1 | default       |                    | 100          | Custom error one   |
            CUSTOM_2 | customMessage | overridden message | 200          | overridden message |
            CUSTOM_1 | withData      | with data          | 100          | with data          | 42
            CUSTOM_2 | withNullData  | msg                | 200          | msg                |
            """)
    void toError(CustomErrors error, String variant, String messageArg, int expectedCode, String expectedMessage, Integer expectedData) {
        Object data = "withData".equals(variant) ? 42 : null;
        IRpcError<?> result;
        if ("default".equals(variant)) {
            result = error.toError();
        } else if ("customMessage".equals(variant)) {
            result = error.toError(messageArg);
        } else if ("withNullData".equals(variant)) {
            result = error.toError(messageArg, null);
        } else {
            result = error.toError(messageArg, data);
        }
        assertEquals(expectedCode, result.code());
        assertEquals(expectedMessage, result.message());
        assertEquals(expectedData, result.data());
    }

    @TableTest("""
            error    | variant       | messageArg | expectedCode | expectedMessage  | expectedData | expectCause
            CUSTOM_1 | default       |            | 100          | Custom error one |              | false
            CUSTOM_2 | customMessage | custom msg | 200          | custom msg       |              | false
            CUSTOM_1 | withData      | data msg   | 100          | data msg         | payload      | false
            CUSTOM_2 | withNullCause | msg        | 200          | msg              |              | false
            CUSTOM_1 | withCause     | wrapped    | 100          | wrapped          |              | true
            """)
    void toException(CustomErrors error, String variant, String messageArg, int expectedCode, String expectedMessage, String expectedData, boolean expectCause) {
        Exception cause = "withCause".equals(variant) ? new IllegalStateException("root") : null;
        Object data = "withData".equals(variant) ? "payload" : null;
        RpcErrorException ex;
        if ("default".equals(variant)) {
            ex = error.toException();
        } else if ("customMessage".equals(variant) || "withNullCause".equals(variant)) {
            ex = error.toException(messageArg);
        } else if ("withCause".equals(variant)) {
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
