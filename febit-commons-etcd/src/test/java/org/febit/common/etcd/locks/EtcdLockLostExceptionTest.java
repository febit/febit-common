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

import org.febit.common.etcd.support.TestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EtcdLockLostExceptionTest {

    private static EtcdLockCredential credential(long leaseId, String key, String grantedKey) {
        return new EtcdLockCredential(leaseId, TestSupport.bytes(key), TestSupport.bytes(grantedKey), 0L);
    }

    @Test
    void exposesReasonAndCredential() {
        var cred = credential(1L, "k", "g");
        var ex = new EtcdLockLostException(EtcdLockLostReason.REMOTE_KEY_MISSING, cred, null);
        assertSame(EtcdLockLostReason.REMOTE_KEY_MISSING, ex.reason());
        assertSame(cred, ex.credential());
        assertTrue(ex.getMessage().contains("REMOTE_KEY_MISSING"));
        assertTrue(ex.getMessage().contains(cred.toString()));
    }

    @Test
    void carriesCause() {
        var cause = new RuntimeException("boom");
        var ex = new EtcdLockLostException(EtcdLockLostReason.KEEP_ALIVE_TERMINATED_AFTER_TTL,
                credential(2L, "k", "g"), cause);
        assertSame(cause, ex.getCause());
    }

    @Test
    void isEtcdLockException() {
        var ex = new EtcdLockLostException(EtcdLockLostReason.UNLOCK_POST_CHECK_LOST,
                credential(3L, "k", "g"), null);
        assertInstanceOf(EtcdLockException.class, ex);
    }
}
