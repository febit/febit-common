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

import io.etcd.jetcd.ByteSequence;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class EtcdLockExceptionTest {

    @Test
    void constructorWithMessage() {
        var ex = new EtcdLockException("test message");
        assertEquals("test message", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void constructorWithMessageAndCause() {
        var cause = new RuntimeException("root cause");
        var ex = new EtcdLockException("test message", cause);
        assertEquals("test message", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void notOwnerExceptionConstructor() {
        var ex = new EtcdLockNotOwnerException("not owner message");
        assertEquals("not owner message", ex.getMessage());
        assertInstanceOf(EtcdLockException.class, ex);
    }

    @Test
    void lostExceptionConstructor() {
        var key = ByteSequence.from("test-key", StandardCharsets.UTF_8);
        var grantedKey = ByteSequence.from("test-granted", StandardCharsets.UTF_8);
        var credential = new EtcdLockCredential(42L, key, grantedKey, 100L);
        var cause = new RuntimeException("cause");
        var ex = new EtcdLockLostException(
                EtcdLockLostReason.REMOTE_KEY_MISSING, credential, cause);

        assertSame(credential, ex.credential());
        assertEquals(EtcdLockLostReason.REMOTE_KEY_MISSING, ex.reason());
        assertSame(cause, ex.getCause());
        assertTrue(ex.getMessage().contains("test-key"));
        assertTrue(ex.getMessage().contains("REMOTE_KEY_MISSING"));
    }

    @Test
    void lostExceptionWithNullCause() {
        var key = ByteSequence.from("test-key", StandardCharsets.UTF_8);
        var grantedKey = ByteSequence.from("test-granted", StandardCharsets.UTF_8);
        var credential = new EtcdLockCredential(42L, key, grantedKey, 100L);
        var ex = new EtcdLockLostException(
                EtcdLockLostReason.KEEP_ALIVE_TERMINATED_AFTER_TTL, credential, null);

        assertNull(ex.getCause());
        assertEquals(EtcdLockLostReason.KEEP_ALIVE_TERMINATED_AFTER_TTL, ex.reason());
    }

    @Test
    void lockLostReasonHasThreeValues() {
        assertEquals(3, EtcdLockLostReason.values().length);
    }
}
