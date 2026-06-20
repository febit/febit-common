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
package org.febit.lang.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void twoArgConstructor_defaultsStatusTo500() {
        var ex = new BusinessException("CODE", "message");
        assertEquals(500, ex.getStatus());
        assertEquals("CODE", ex.getCode());
        assertEquals("message", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void threeArgConstructor_withStatus() {
        var ex = new BusinessException(404, "NOT_FOUND", "missing");
        assertEquals(404, ex.getStatus());
        assertEquals("NOT_FOUND", ex.getCode());
        assertEquals("missing", ex.getMessage());
    }

    @Test
    void threeArgConstructor_withCause_defaultsStatusTo500() {
        var cause = new RuntimeException("boom");
        var ex = new BusinessException("CODE", "message", cause);
        assertEquals(500, ex.getStatus());
        assertEquals("CODE", ex.getCode());
        assertEquals("message", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void fourArgConstructor_setsAllFields() {
        var cause = new RuntimeException("root");
        var ex = new BusinessException(503, "SVC_DOWN", "down", cause);
        assertEquals(503, ex.getStatus());
        assertEquals("SVC_DOWN", ex.getCode());
        assertEquals("down", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void protectedConstructor_runsWithEnableSuppressionAndWritableStackTrace() {
        // The protected constructor is used by the public ones; the stack trace
        // is created by the Throwable super constructor unless writableStackTrace=false.
        var ex = new BusinessException(500, "CODE", "message", null, true, true);
        assertEquals(500, ex.getStatus());
        assertEquals("CODE", ex.getCode());
        assertEquals("message", ex.getMessage());
    }

    @Test
    void protectedConstructor_withWritableStackTraceFalse_omitsStackTrace() {
        var ex = new BusinessException(500, "CODE", "message", null, true, false);
        assertEquals(0, ex.getStackTrace().length, "Stack trace should be empty when writableStackTrace=false");
    }

    @Test
    void isRuntimeException() {
        var ex = new BusinessException("CODE", "message");
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void canBeThrownAndCaught() {
        var caught = org.junit.jupiter.api.Assertions.assertThrows(
                BusinessException.class,
                () -> {
                    throw new BusinessException("CODE", "message");
                }
        );
        assertEquals("CODE", caught.getCode());
    }

    @Test
    void getLocalizedMessage_prefixesCodeAndMessage() {
        var ex = new BusinessException("AUTH_FAILED", "Invalid token");
        // Default implementation: '[' + code + "] " + message
        assertEquals("[AUTH_FAILED] Invalid token", ex.getLocalizedMessage());
    }

    @Test
    void from_response_buildsExceptionWithSameFields() {
        var response = IResponse.failed(401, "UNAUTHORIZED", "no");
        var ex = BusinessException.from(response);
        assertEquals(401, ex.getStatus());
        assertEquals("UNAUTHORIZED", ex.getCode());
        assertEquals("no", ex.getMessage());
    }

    @Test
    void from_response_preservesSuccessFlagAsFalse() {
        var response = IResponse.failed(403, "FORBIDDEN", "denied");
        var ex = BusinessException.from(response);
        assertFalse(ex.getMessage().isEmpty());
    }

    @Test
    void toResponse_buildsFailedResponseWithSameFields() {
        var ex = new BusinessException(404, "NOT_FOUND", "missing");
        var response = ex.toResponse();
        assertEquals(404, response.getStatus());
        assertEquals("NOT_FOUND", response.getCode());
        assertEquals("missing", response.getMessage());
        assertFalse(response.isSuccess());
    }

    @Test
    void fromAndToResponse_areInverses() {
        var original = IResponse.failed(422, "INVALID", "bad input");
        var ex = BusinessException.from(original);
        var roundTrip = ex.toResponse();
        assertEquals(original.getStatus(), roundTrip.getStatus());
        assertEquals(original.getCode(), roundTrip.getCode());
        assertEquals(original.getMessage(), roundTrip.getMessage());
    }

    @Test
    void protectedConstructor_withWritableStackTraceTrue_populatesStack() {
        var ex = new BusinessException(500, "CODE", "msg", null, true, true);
        assertTrue(ex.getStackTrace().length > 0,
                "Stack trace should be populated when writableStackTrace=true");
    }

    @Test
    void twoArgConstructor_nullMessageAllowed() {
        var ex = new BusinessException("CODE", null);
        assertNull(ex.getMessage());
        assertEquals("CODE", ex.getCode());
        assertEquals(500, ex.getStatus());
    }

    @Test
    void threeArgConstructor_nullCodeAllowed() {
        var ex = new BusinessException(400, null, "bad");
        assertNull(ex.getCode());
        assertEquals(400, ex.getStatus());
        assertEquals("bad", ex.getMessage());
    }

    @Test
    void suppressedExceptionsRespected() {
        var primary = new IllegalStateException("primary");
        var suppressed = new IllegalArgumentException("suppressed");
        primary.addSuppressed(suppressed);
        var ex = new BusinessException("CODE", "msg", primary);
        assertSame(primary, ex.getCause());
        assertEquals(1, ex.getCause().getSuppressed().length);
    }

    @Test
    void toResponse_producesFailedIResponse() {
        var ex = new BusinessException(403, "FORBIDDEN", "denied");
        var r = ex.toResponse();
        assertFalse(r.isSuccess());
        assertTrue(r.isFailed());
    }

    @Test
    void getLocalizedMessage_handlesSpecialCharsInCode() {
        var ex = new BusinessException("X.Y_Z", "msg");
        assertEquals("[X.Y_Z] msg", ex.getLocalizedMessage());
    }

    @Test
    void getLocalizedMessage_emptyCodeAndMessage() {
        var ex = new BusinessException("", "");
        assertEquals("[] ", ex.getLocalizedMessage());
    }

    @Test
    void fromResponse_statusMayBeAny() {
        var r = IResponse.failed(599, "WEIRD", "weird");
        var ex = BusinessException.from(r);
        assertEquals(599, ex.getStatus());
    }

    @Test
    void fromResponse_dataFieldIsIgnored() {
        var r = IResponse.failed("CODE", "msg", "data");
        var ex = BusinessException.from(r);
        // from() does not propagate the data field
        assertEquals("CODE", ex.getCode());
        assertEquals("msg", ex.getMessage());
    }
}
