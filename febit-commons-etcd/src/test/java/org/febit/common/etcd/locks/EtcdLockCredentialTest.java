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

class EtcdLockCredentialTest {

    @Test
    void recordComponentsAccessible() {
        var key = ByteSequence.from("key", StandardCharsets.UTF_8);
        var grantedKey = ByteSequence.from("granted", StandardCharsets.UTF_8);
        var credential = new EtcdLockCredential(123L, key, grantedKey, 456L);

        assertEquals(123L, credential.leaseId());
        assertSame(key, credential.key());
        assertSame(grantedKey, credential.grantedKey());
        assertEquals(456L, credential.fencingToken());
    }

    @Test
    void toStringContainsComponents() {
        var key = ByteSequence.from("key", StandardCharsets.UTF_8);
        var grantedKey = ByteSequence.from("granted", StandardCharsets.UTF_8);
        var credential = new EtcdLockCredential(123L, key, grantedKey, 456L);

        var str = credential.toString();
        assertTrue(str.contains("123"));
        assertTrue(str.contains("456"));
    }

    @Test
    void equalsAndHashCodeSameValues() {
        var key = ByteSequence.from("key", StandardCharsets.UTF_8);
        var grantedKey = ByteSequence.from("granted", StandardCharsets.UTF_8);
        var a = new EtcdLockCredential(1L, key, grantedKey, 10L);
        var b = new EtcdLockCredential(1L, key, grantedKey, 10L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsDifferentValuesNotEqual() {
        var key = ByteSequence.from("key", StandardCharsets.UTF_8);
        var grantedKey = ByteSequence.from("granted", StandardCharsets.UTF_8);
        var a = new EtcdLockCredential(1L, key, grantedKey, 10L);
        var b = new EtcdLockCredential(2L, key, grantedKey, 10L);

        assertNotEquals(a, b);
    }
}
