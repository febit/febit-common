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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.febit.common.etcd.support.TestSupport.DU_60S;
import static org.junit.jupiter.api.Assertions.*;

class DeadlineTest {

    @Test
    void remainingWhenExpired() {
        var deadline = new Deadline(System.nanoTime() - 1);
        assertThatThrownBy(deadline::remaining)
                .isInstanceOf(TimeoutException.class);
    }

    @Test
    void ofWithDuration() throws TimeoutException {
        var deadline = Deadline.of(DU_60S);
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
