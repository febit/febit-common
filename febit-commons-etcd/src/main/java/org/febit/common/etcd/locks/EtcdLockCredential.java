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

/**
 * Immutable snapshot of a granted lock hold.
 *
 * @param leaseId      etcd lease ID backing this lock
 * @param key          the original lock key requested
 * @param grantedKey   the actual etcd lock key returned by the server
 * @param fencingToken the create-revision of {@code grantedKey}, used to detect stale holders
 */
public record EtcdLockCredential(
        long leaseId,
        ByteSequence key,
        ByteSequence grantedKey,
        long fencingToken
) {
}
