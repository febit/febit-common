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
package org.febit.common.etcd.locks;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class EtcdLockSupportTest {

    @Test
    void notOwnerExceptionMessageAndType() {
        var ex = EtcdLockSupport.notOwner("test-key");
        assertInstanceOf(EtcdLockNotOwnerException.class, ex);
        assertTrue(ex.getMessage().contains("Current thread does not hold lock for test-key"));
    }

    @Test
    void unwrapReturnsExecutionExceptionWhenCauseIsNull() {
        var noCauseEE = new ExecutionException("no cause", null);
        assertSame(noCauseEE, EtcdLockSupport.unwrap(noCauseEE));
    }

    @Test
    void mergeFailureReturnsNextWhenCurrentIsNull() {
        var next = new RuntimeException("next");
        assertSame(next, EtcdLockSupport.mergeFailure(null, next));
    }

    @Test
    void mergeFailureReturnsCurrentWhenNextIsNull() {
        var current = new RuntimeException("current");
        assertSame(current, EtcdLockSupport.mergeFailure(current, null));
    }

    @Test
    void mergeFailureAddsSuppressedWhenBothNonNull() {
        var current = new RuntimeException("current");
        var next = new RuntimeException("next");
        var result = EtcdLockSupport.mergeFailure(current, next);
        assertSame(current, result);
        assertEquals(1, result.getSuppressed().length);
        assertSame(next, result.getSuppressed()[0]);
    }

    @Test
    void unwrapReturnsCauseWhenNonNull() {
        var cause = new RuntimeException("cause");
        var ee = new ExecutionException(cause);
        assertSame(cause, EtcdLockSupport.unwrap(ee));
    }
}
