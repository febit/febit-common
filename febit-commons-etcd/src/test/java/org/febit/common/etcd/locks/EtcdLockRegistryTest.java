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
import io.etcd.jetcd.Client;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class EtcdLockRegistryTest {

    @Test
    void builderRejectsNullClient() {
        assertThrows(NullPointerException.class, () ->
                EtcdLockRegistry.builder().build());
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
    void heldByCurrentThreadReturnsEmptyBeforeAnyLock() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client)) {
            var held = registry.heldByCurrentThread();
            assertNotNull(held);
            assertTrue(held.isEmpty());
        }
    }

    @Test
    void isHeldByCurrentThreadReturnsFalseBeforeAnyLock() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client)) {
            assertFalse(registry.isHeldByCurrentThread(
                    List.of(bytes("not-held"))));
        }
    }

    @Test
    void lockForByteSequenceCreatesLock() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client)) {
            var key = ByteSequence.from("some-key", StandardCharsets.UTF_8);
            var lock = registry.lockFor(key);
            assertNotNull(lock);
            assertEquals(List.of(key), ((EtcdLockImpl) lock).keys());
        }
    }

    @Test
    void lockForVarargsCreatesMultiKeyLock() {
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
    void lockForListOfStringsCreatesLock() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client)) {
            var lock = (EtcdLockImpl) registry.lockFor(List.of("list/a", "list/b"));
            assertEquals(2, lock.keys().size());
            assertEquals("list/a", lock.keys().get(0).toString());
            assertEquals("list/b", lock.keys().get(1).toString());
        }
    }

    @Test
    void lockForBytesCreatesLock() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client)) {
            var keys = List.of(bytes("bytes/key"));
            var lock = (EtcdLockImpl) registry.lockForBytes(keys);
            assertEquals(1, lock.keys().size());
            assertEquals("bytes/key", lock.keys().get(0).toString());
        }
    }

    @Test
    void builderSupportsWaitMax() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var waitMax = Duration.ofMinutes(10);
            var registry = EtcdLockRegistry.builder()
                    .client(client)
                    .waitMax(waitMax)
                    .build();
            assertEquals(waitMax, registry.options().waitMax());
        }
    }

    @Test
    void createWithOptionsAcceptsClientAndOptions() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var options = EtcdLockOptions.builder()
                    .strict(true)
                    .ttl(Duration.ofSeconds(15))
                    .build();
            var registry = EtcdLockRegistry.create(client, options);
            assertTrue(registry.options().strict());
            assertEquals(Duration.ofSeconds(15), registry.options().ttl());
        }
    }

    private static ByteSequence bytes(String value) {
        return ByteSequence.from(value, StandardCharsets.UTF_8);
    }
}
