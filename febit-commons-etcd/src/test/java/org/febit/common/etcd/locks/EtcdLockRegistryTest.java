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

import io.etcd.jetcd.Client;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.febit.common.etcd.support.TestSupport.DU_10M;
import static org.febit.common.etcd.support.TestSupport.DU_15S;
import static org.febit.common.etcd.support.TestSupport.bytes;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class EtcdLockRegistryTest {

    @Test
    void builderRejectsNullClient() {
        assertThatThrownBy(() -> EtcdLockRegistry.builder().build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createAcceptsClient() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var registry = EtcdLockRegistry.create(client);
            assertNotNull(registry);
            assertNotNull(registry.options());
        }
    }

    @Test
    void heldByCurrentThreadInitially() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client)) {
            var held = registry.heldByCurrentThread();
            assertNotNull(held);
            assertTrue(held.isEmpty());
        }
    }

    @Test
    void isHeldByCurrentThreadInitially() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client)) {
            assertFalse(registry.isHeldByCurrentThread(
                    List.of(bytes("not-held"))));
        }
    }

    @Test
    void lockForBytes() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client)) {
            var key = bytes("some-key");
            var lock = registry.lockFor(key);
            assertNotNull(lock);
            assertEquals(List.of(key), ((EtcdLockImpl) lock).keys());
        }
    }

    @Test
    void lockForVarargs() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client)) {
            var lock = (EtcdLockImpl) registry.lockFor("var/a", "var/b");
            assertEquals(2, lock.keys().size());
            assertEquals("var/a", lock.keys().get(0).toString());
            assertEquals("var/b", lock.keys().get(1).toString());
        }
    }

    @Test
    void closeDoesNotThrow() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client)) {
            assertDoesNotThrow(registry::close);
        }
    }

    @Test
    void lockForList() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client)) {
            var lock = (EtcdLockImpl) registry.lockFor(List.of("list/a", "list/b"));
            assertEquals(2, lock.keys().size());
            assertEquals("list/a", lock.keys().get(0).toString());
            assertEquals("list/b", lock.keys().get(1).toString());
        }
    }

    @Test
    void lockForBytesList() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client)
        ) {
            var keys = List.of(bytes("bytes/key"));
            var lock = (EtcdLockImpl) registry.lockForBytes(keys);
            assertEquals(1, lock.keys().size());
            assertEquals("bytes/key", lock.keys().getFirst().toString());
        }
    }

    @Test
    void waitMaxOption() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.builder()
                     .client(client)
                     .waitMax(DU_10M)
                     .build()
        ) {
            assertEquals(DU_10M, registry.options().waitMax());
        }
    }

    @Test
    void createWithOptions() {
        var options = EtcdLockOptions.builder()
                .strict(true)
                .ttl(DU_15S)
                .build();
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client, options)
        ) {
            assertTrue(registry.options().strict());
            assertEquals(DU_15S, registry.options().ttl());
        }
    }
}
