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
package org.febit.common.etcd.store;

import org.junit.jupiter.api.Test;

import org.febit.lang.UncheckedException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionUtilsTest {

    @Test
    void unwrapPassesThroughRuntimeException() {
        var re = new IllegalStateException("boom");
        assertSame(re, ExceptionUtils.unwrap(re, "msg"));
    }

    @Test
    void unwrapWrapsCheckedExceptionAsUnchecked() {
        var io = new IOException("disk");
        var out = ExceptionUtils.unwrap(io, "failed");

        assertInstanceOf(UncheckedException.class, out);
        assertEquals("failed", out.getMessage());
        assertSame(io, out.getCause());
    }

    @Test
    void unwrapUnwrapsExecutionExceptionToCause() {
        var root = new RuntimeException("root");
        var exec = new ExecutionException(root);

        assertSame(root, ExceptionUtils.unwrap(exec, "ctx"));
    }

    @Test
    void unwrapRestoresInterruptFlagForInterruptedException() {
        Thread.interrupted(); // ensure clean baseline
        var out = ExceptionUtils.unwrap(new InterruptedException("stop"), "ctx");

        assertInstanceOf(UncheckedException.class, out);
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted(); // reset for subsequent tests
    }

    @Test
    void unwrapUnwrapsNestedExecutionException() {
        var root = new IllegalStateException("deep");
        var exec = new ExecutionException(new ExecutionException(root));

        assertSame(root, ExceptionUtils.unwrap(exec, "ctx"));
    }
}
