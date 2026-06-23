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
package org.febit.common.jsonrpc2.protocol;

import org.febit.common.jsonrpc2.internal.protocol.Request;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IRpcRequestTest {

    @Nested
    class AsNotification {

        @Test
        void copiesMethodAndParams() {
            var request = new Request(Id.of(1), "test/method", "payload");
            var notification = request.asNotification();

            assertEquals("test/method", notification.method());
            assertEquals("payload", notification.params());
        }

        @Test
        void hasNullId() {
            var request = new Request(Id.of(1), "test", null);
            var notification = request.asNotification();

            assertNull(notification.id());
        }

        @Test
        void withNullParams() {
            var request = new Request(Id.of(1), "event", null);
            var notification = request.asNotification();

            assertEquals("event", notification.method());
            assertNull(notification.params());
        }
    }

    @Nested
    class BasicProperties {

        @Test
        void idIsNonNull() {
            var request = new Request(Id.of(42), "m", null);
            assertEquals(Id.of(42), request.id());
        }

        @Test
        void methodIsNonNull() {
            var request = new Request(Id.of("abc"), "theMethod", null);
            assertEquals("theMethod", request.method());
        }

        @Test
        void paramsCanBeNull() {
            var request = new Request(Id.of(1), "m", null);
            assertNull(request.params());
        }
    }
}
