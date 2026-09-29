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

import org.junit.jupiter.api.Nested;
import org.tabletest.junit.TableTest;

import org.febit.common.jsonrpc2.exception.RpcErrorException;

import static org.junit.jupiter.api.Assertions.*;

class ReservedRpcErrorsTest {

    @Nested
    class EnumValues {

        @TableTest("""
                error       | code   | message            | descriptionContains
                RESERVED_00 | -32000 | Server error 32000 | implementation-defined
                RESERVED_99 | -32099 | Server error 32099 | implementation-defined
                """)
        void definesCodeMessageAndDescription(ReservedRpcErrors error, int code, String message, String descriptionContains) {
            assertEquals(code, error.code());
            assertEquals(message, error.message());
            assertTrue(error.description().contains(descriptionContains));
        }
    }

    @TableTest("""
            error       | variant | messageArg | expectedCode | expectedMessage
            RESERVED_00 | default |            | -32000       | Server error 32000
            RESERVED_99 | message | custom     | -32099       | custom
            """)
    void toError(ReservedRpcErrors error, String variant, String messageArg, int expectedCode, String expectedMessage) {
        IRpcError<?> result;
        if ("default".equals(variant)) {
            result = error.toError();
        } else {
            result = error.toError(messageArg);
        }
        assertEquals(expectedCode, result.code());
        assertEquals(expectedMessage, result.message());
        assertNull(result.data());
    }

    @TableTest("""
            error       | variant | messageArg   | expectedCode | expectedMessage    | expectCause
            RESERVED_00 | default |              | -32000       | Server error 32000 | false
            RESERVED_99 | message | server error | -32099       | server error       | false
            RESERVED_00 | cause   | wrapped      | -32000       | wrapped            | true
            """)
    void toException(ReservedRpcErrors error, String variant, String messageArg, int expectedCode, String expectedMessage, boolean expectCause) {
        Exception cause = "cause".equals(variant) ? new RuntimeException("root") : null;
        RpcErrorException ex;
        if ("default".equals(variant)) {
            ex = error.toException();
        } else if ("message".equals(variant)) {
            ex = error.toException(messageArg);
        } else {
            ex = error.toException(messageArg, cause);
        }
        assertEquals(expectedCode, ex.getError().code());
        assertEquals(expectedMessage, ex.getError().message());
        if (expectCause) {
            assertEquals(cause, ex.getCause());
        } else {
            assertNull(ex.getCause());
        }
    }
}
