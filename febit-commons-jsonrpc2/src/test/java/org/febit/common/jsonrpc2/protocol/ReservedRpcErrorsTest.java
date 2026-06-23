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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReservedRpcErrorsTest {

    @Nested
    class EnumValues {

        @Test
        void reserved00Code() {
            assertEquals(-32000, ReservedRpcErrors.RESERVED_00.code());
        }

        @Test
        void reserved00Message() {
            assertEquals("Server error 32000", ReservedRpcErrors.RESERVED_00.message());
        }

        @Test
        void reserved00Description() {
            assertTrue(ReservedRpcErrors.RESERVED_00.description()
                    .contains("implementation-defined"));
        }

        @Test
        void reserved99Code() {
            assertEquals(-32099, ReservedRpcErrors.RESERVED_99.code());
        }

        @Test
        void reserved99Message() {
            assertEquals("Server error 32099", ReservedRpcErrors.RESERVED_99.message());
        }

        @Test
        void reserved99Description() {
            assertTrue(ReservedRpcErrors.RESERVED_99.description()
                    .contains("implementation-defined"));
        }
    }

    @Nested
    class ToError {

        @Test
        void toErrorDefault() {
            var error = ReservedRpcErrors.RESERVED_00.toError();
            assertEquals(-32000, error.code());
            assertEquals("Server error 32000", error.message());
            assertNull(error.data());
        }

        @Test
        void toErrorWithMessage() {
            var error = ReservedRpcErrors.RESERVED_99.toError("custom");
            assertEquals(-32099, error.code());
            assertEquals("custom", error.message());
        }
    }

    @Nested
    class ToException {

        @Test
        void toExceptionDefault() {
            var ex = ReservedRpcErrors.RESERVED_00.toException();
            assertEquals(-32000, ex.getError().code());
        }

        @Test
        void toExceptionWithMessage() {
            var ex = ReservedRpcErrors.RESERVED_99.toException("server error");
            assertEquals(-32099, ex.getError().code());
            assertEquals("server error", ex.getError().message());
        }

        @Test
        void toExceptionWithCause() {
            var cause = new RuntimeException("root");
            var ex = ReservedRpcErrors.RESERVED_00.toException("wrapped", cause);
            assertEquals(cause, ex.getCause());
        }
    }
}
