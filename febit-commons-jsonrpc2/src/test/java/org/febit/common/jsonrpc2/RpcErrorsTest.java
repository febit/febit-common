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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

    @Nested
    class ToError {

        @Test
        void defaultToErrorUsesDefaultMessage() {
            var error = CustomErrors.CUSTOM_1.toError();
            assertEquals(100, error.code());
            assertEquals("Custom error one", error.message());
            assertNull(error.data());
        }

        @Test
        void toErrorWithCustomMessage() {
            var error = CustomErrors.CUSTOM_2.toError("overridden message");
            assertEquals(200, error.code());
            assertEquals("overridden message", error.message());
            assertNull(error.data());
        }

        @Test
        void toErrorWithData() {
            var error = CustomErrors.CUSTOM_1.toError("with data", 42);
            assertEquals(100, error.code());
            assertEquals("with data", error.message());
            assertEquals(42, error.data());
        }

        @Test
        void toErrorWithNullData() {
            var error = CustomErrors.CUSTOM_2.toError("msg", null);
            assertEquals(200, error.code());
            assertEquals("msg", error.message());
            assertNull(error.data());
        }
    }

    @Nested
    class ToException {

        @Test
        void defaultToExceptionUsesDefaultMessage() {
            var ex = CustomErrors.CUSTOM_1.toException();
            assertNotNull(ex);
            assertEquals(100, ex.getError().code());
            assertEquals("Custom error one", ex.getError().message());
        }

        @Test
        void toExceptionWithCustomMessageNoCause() {
            var ex = CustomErrors.CUSTOM_2.toException("custom message");
            assertNotNull(ex);
            assertEquals(200, ex.getError().code());
            assertEquals("custom message", ex.getError().message());
            assertNull(ex.getCause());
        }

        @Test
        void toExceptionWithMessageAndCause() {
            var cause = new IllegalStateException("root");
            var ex = CustomErrors.CUSTOM_1.toException("wrapped", cause);

            assertEquals(100, ex.getError().code());
            assertEquals("wrapped", ex.getError().message());
            assertEquals(cause, ex.getCause());
        }

        @Test
        void toExceptionWithNullCause() {
            var ex = CustomErrors.CUSTOM_2.toException("msg", null);

            assertEquals(200, ex.getError().code());
            assertEquals("msg", ex.getError().message());
            assertNull(ex.getCause());
        }

        @Test
        void toExceptionWithMessageAndData() {
            var ex = CustomErrors.CUSTOM_1.toException("data msg", "payload");

            assertEquals(100, ex.getError().code());
            assertEquals("data msg", ex.getError().message());
            assertEquals("payload", ex.getError().data());
        }
    }

    @Nested
    class CustomErrorsValues {

        @Test
        void codeMatches() {
            assertEquals(100, CustomErrors.CUSTOM_1.code());
            assertEquals(200, CustomErrors.CUSTOM_2.code());
        }

        @Test
        void messageMatches() {
            assertEquals("Custom error one", CustomErrors.CUSTOM_1.message());
            assertEquals("Custom error two", CustomErrors.CUSTOM_2.message());
        }

        @Test
        void descriptionMatches() {
            assertEquals("Description one", CustomErrors.CUSTOM_1.description());
            assertEquals("Description two", CustomErrors.CUSTOM_2.description());
        }
    }
}
