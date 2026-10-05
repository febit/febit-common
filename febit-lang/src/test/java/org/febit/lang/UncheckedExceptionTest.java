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
package org.febit.lang;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class UncheckedExceptionTest {

    @Test
    void noArgsConstructor() {
        var e = new UncheckedException();
        assertNull(e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    void messageOnly() {
        var e = new UncheckedException("boom");
        assertEquals("boom", e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    void causeOnly() {
        var cause = new IOException("io");
        var e = new UncheckedException(cause);
        assertSame(cause, e.getCause());
        assertTrue(e.getMessage().contains("io"));
    }

    @Test
    void messageAndCause() {
        var cause = new IOException("io");
        var e = new UncheckedException("wrap", cause);
        assertEquals("wrap", e.getMessage());
        assertSame(cause, e.getCause());
    }

    @Test
    void suppressionDisabled() {
        var cause = new IOException("io");
        var e = new UncheckedException("wrap", cause, false, false);
        assertEquals("wrap", e.getMessage());
        assertSame(cause, e.getCause());
        assertEquals(0, e.getSuppressed().length);
    }
}
