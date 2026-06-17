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
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Getter
@Accessors(fluent = true)
public class EtcdLockRegistry implements AutoCloseable {

    private final ThreadLocal<EtcdLockLocalGuard> guards = ThreadLocal.withInitial(() -> new EtcdLockLocalGuard(this));

    private final Client client;
    private final EtcdLockOptions options;

    private EtcdLockRegistry(Client client, EtcdLockOptions options) {
        this.client = client;
        this.options = options;
    }

    EtcdLockLocalGuard localGuard() {
        return guards.get();
    }

    public static EtcdLockRegistry create(Client client) {
        return create(client, EtcdLockOptions.defaults());
    }

    public static EtcdLockRegistry create(Client client, EtcdLockOptions options) {
        return new EtcdLockRegistry(client, options);
    }

    @lombok.Builder(
            builderClassName = "Builder"
    )
    private static EtcdLockRegistry createForBuilder(
            @lombok.NonNull Client client,
            @Nullable Boolean strict,
            @Nullable Duration ttl,
            @Nullable Duration tryLockTimeout,
            @Nullable Duration waitMax
    ) {
        var options = EtcdLockOptions.builder();
        if (strict != null) {
            options.strict(strict);
        }
        if (ttl != null) {
            options.ttl(ttl);
        }
        if (tryLockTimeout != null) {
            options.tryLockTimeout(tryLockTimeout);
        }
        if (waitMax != null) {
            options.waitMax(waitMax);
        }
        return create(client, options.build());
    }

    private static ByteSequence normalizeKey(String key) {
        return ByteSequence.from(key, StandardCharsets.UTF_8);
    }

    public List<EtcdLockCredential> heldByCurrentThread() {
        return localGuard().holds();
    }

    public boolean isHeldByCurrentThread(List<ByteSequence> keys) {
        return localGuard().isHeldAll(keys);
    }

    public EtcdLock lockForBytes(List<ByteSequence> keys) {
        return new EtcdLockImpl(this, keys);
    }

    public EtcdLock lockFor(String key) {
        return lockFor(ByteSequence.from(key.getBytes()));
    }

    public EtcdLock lockFor(ByteSequence key) {
        return lockForBytes(List.of(key));
    }

    public EtcdLock lockFor(String... keys) {
        return lockFor(Arrays.asList(keys));
    }

    public EtcdLock lockFor(List<String> keys) {
        var normalized = new ByteSequence[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            normalized[i] = normalizeKey(keys.get(i));
        }
        return lockForBytes(List.of(normalized));
    }

    @Override
    public void close() {
    }
}
