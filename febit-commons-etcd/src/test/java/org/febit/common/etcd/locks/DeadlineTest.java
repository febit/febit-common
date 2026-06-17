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

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class DeadlineTest {

    @Test
    void remainingWhenExpired() {
        var deadline = new Deadline(System.nanoTime() - 1);
        assertThrows(TimeoutException.class, deadline::remaining);
    }

    @Test
    void ofWithDurationReturnsDeadlineOfAtLeastGivenDuration() throws TimeoutException {
        var duration = Duration.ofSeconds(60);
        var deadline = Deadline.of(duration);
        assertNotNull(deadline);
        assertTrue(deadline.remaining() >= 0);
    }

    @Test
    void ofNullReturnsNull() {
        assertNull(Deadline.of(null));
    }

    @Test
    void ofNegativeDurationReturnsNull() {
        assertNull(Deadline.of(Duration.ofSeconds(-1)));
    }
}
