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
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

@Slf4j
@UtilityClass
public class EtcdLockSupport {

    static void unlock(EtcdLease lease, EtcdLockCredential credential) {
        var loss = detectLockLoss(lease, List.of(credential));
        if (loss.isPresent()) {
            throw new EtcdLockLostException(loss.get(), credential, null);
        }
        try {
            lease.client()
                    .getLockClient()
                    .unlock(credential.grantedKey())
                    .get();
            log.debug("Unlocked key: {}", credential);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Interrupted while unlocking key: {}", credential, e);
            throw new EtcdLockException("Interrupted while unlocking key: " + credential, e);
        } catch (ExecutionException e) {
            log.debug("Unlock failed for key: {}, checking lock status...", credential);
            loss = detectLockLoss(lease, List.of(credential));
            if (loss.isPresent()) {
                log.debug("Lock already lost for key: {}, reason: {}", credential, loss.get());
                throw new EtcdLockLostException(
                        EtcdLockLostReason.UNLOCK_POST_CHECK_LOST, credential, unwrap(e));
            }
            log.debug("Unlock failed for key: {}", credential, unwrap(e));
            throw new EtcdLockException("Failed to unlock key: " + credential, unwrap(e));
        }
    }

    static Optional<EtcdLockLostReason> detectLockLoss(EtcdLease lease, List<EtcdLockCredential> credentials) {
        var client = lease.client();
        if (lease.isDefinitelyLost()) {
            return Optional.of(EtcdLockLostReason.KEEP_ALIVE_TERMINATED_AFTER_TTL);
        }
        for (var credential : credentials) {
            try {
                if (detectKeyMissing(client, credential)) {
                    return Optional.of(EtcdLockLostReason.REMOTE_KEY_MISSING);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new EtcdLockException("Interrupted while checking remote lock: " + credential, e);
            } catch (ExecutionException e) {
                throw new EtcdLockException("Failed to check remote lock: " + credential, unwrap(e));
            } catch (RejectedExecutionException e) {
                if (lease.isDefinitelyLost()) {
                    return Optional.of(EtcdLockLostReason.KEEP_ALIVE_TERMINATED_AFTER_TTL);
                }
                throw new EtcdLockException("Failed to check remote lock: " + credential, e);
            } catch (RuntimeException e) {
                if (lease.isDefinitelyLost()) {
                    return Optional.of(EtcdLockLostReason.KEEP_ALIVE_TERMINATED_AFTER_TTL);
                }
                throw e;
            }
        }
        return Optional.empty();
    }

    private static boolean detectKeyMissing(Client client, EtcdLockCredential credential)
            throws ExecutionException, InterruptedException {
        return client.getKVClient()
                .get(credential.grantedKey())
                .get()
                .getKvs()
                .isEmpty();
    }

    static EtcdLockNotOwnerException notOwner(String description) {
        return new EtcdLockNotOwnerException("Current thread does not hold lock for " + description);
    }

    static Throwable unwrap(ExecutionException e) {
        return e.getCause() != null ? e.getCause() : e;
    }

    @Nullable
    static Exception mergeFailure(@Nullable Exception current, @Nullable Exception next) {
        if (current == null) {
            return next;
        }
        if (next != null) {
            current.addSuppressed(next);
        }
        return current;
    }
}
