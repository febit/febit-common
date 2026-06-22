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
package org.febit.common.etcd.support;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.options.GetOption;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@UtilityClass
public final class EtcdIntegrationTestSupport {

    public static final GetOption PREFIX_GET_OPTION = GetOption.builder().isPrefix(true).build();

    public static void awaitQueuedContender(Client client, ByteSequence lockName, Duration timeout) throws InterruptedException {
        awaitCondition(timeout,
                () -> lockQueueDepth(client, lockName) >= 2,
                () -> "Timed out waiting for queued contender for key: " + lockName);
    }

    public static void awaitLockKeyDeletion(Client client, ByteSequence lockKey, Duration timeout) throws InterruptedException {
        awaitCondition(timeout,
                () -> !lockKeyExists(client, lockKey),
                () -> "Timed out waiting for lock key to disappear: " + lockKey);
    }

    public static void awaitCondition(Duration timeout, CheckedBooleanSupplier condition, MessageSupplier timeoutMessage)
            throws InterruptedException {
        var deadlineNanos = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadlineNanos) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(20L);
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
        fail(timeoutMessage.get());
    }

    public static boolean lockKeyExists(Client client, ByteSequence lockKey) {
        try {
            return !client.getKVClient().get(lockKey)
                    .get(1, TimeUnit.SECONDS)
                    .getKvs()
                    .isEmpty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while querying lock key: " + lockKey, e);
        } catch (Exception e) {
            throw new AssertionError("Failed to query lock key: " + lockKey, e);
        }
    }

    public static int lockQueueDepth(Client client, ByteSequence lockName) {
        var prefix = lockName.concat(TestSupport.LOCK_KEY_DELIMITER);
        try {
            return client.getKVClient().get(prefix, PREFIX_GET_OPTION)
                    .get(1, TimeUnit.SECONDS)
                    .getKvs()
                    .size();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while querying queued locks for key: " + lockName, e);
        } catch (Exception e) {
            throw new AssertionError("Failed to query queued locks for key: " + lockName, e);
        }
    }

    @FunctionalInterface
    public interface CheckedBooleanSupplier {

        boolean getAsBoolean() throws InterruptedException;
    }

    @FunctionalInterface
    public interface MessageSupplier {

        String get();
    }
}

