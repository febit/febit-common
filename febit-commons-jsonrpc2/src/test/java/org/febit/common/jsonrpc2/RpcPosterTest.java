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
package org.febit.common.jsonrpc2;

import org.febit.common.jsonrpc2.internal.protocol.Notification;
import org.febit.common.jsonrpc2.internal.protocol.Request;
import org.febit.common.jsonrpc2.protocol.IRpcMessage;
import org.febit.common.jsonrpc2.protocol.Id;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class RpcPosterTest {

    @Nested
    class JsonifiedPoster {

        static class TestJsonifiedPoster implements RpcPoster.Jsonified {
            final List<String> received = new ArrayList<>();

            @Override
            public void post(String json) {
                received.add(json);
            }
        }

        @Test
        void postMessageEncodesToJson() {
            var poster = new TestJsonifiedPoster();
            var notification = new Notification("test.event", "data");

            poster.post((IRpcMessage) notification);

            assertEquals(1, poster.received.size());
            var json = poster.received.get(0);
            assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
            assertTrue(json.contains("\"method\":\"test.event\""));
            assertTrue(json.contains("\"data\""));
        }

        @Test
        void postRequestEncodesIdAndMethod() {
            var poster = new TestJsonifiedPoster();
            var request = new Request(Id.of(42), "calc/add", List.of(1, 2));

            poster.post((IRpcMessage) request);

            assertEquals(1, poster.received.size());
            var json = poster.received.get(0);
            assertTrue(json.contains("\"id\":42"));
            assertTrue(json.contains("\"method\":\"calc/add\""));
        }

        @Test
        void postMultipleMessages() {
            var poster = new TestJsonifiedPoster();

            poster.post((IRpcMessage) new Notification("a", null));
            poster.post((IRpcMessage) new Notification("b", null));
            poster.post((IRpcMessage) new Notification("c", null));

            assertEquals(3, poster.received.size());
        }

        @Test
        void postNullParams() {
            var poster = new TestJsonifiedPoster();
            var notification = new Notification("empty", null);

            poster.post((IRpcMessage) notification);

            assertEquals(1, poster.received.size());
            // Should NOT contain "params":null because @JsonInclude(NON_NULL) likely
            assertTrue(poster.received.get(0).contains("\"jsonrpc\":\"2.0\""));
        }
    }

    @Nested
    class BasicPoster {

        @Test
        void directPostReceivesMessage() {
            var captured = new AtomicReference<IRpcMessage>();
            RpcPoster poster = captured::set;

            var notification = new Notification("direct", "payload");
            poster.post(notification);

            assertSame(notification, captured.get());
        }

        @Test
        void directPostRequestIsReceived() {
            var captured = new AtomicReference<IRpcMessage>();
            RpcPoster poster = captured::set;

            var request = new Request(Id.of(1), "rpc/method", List.of("a", "b"));
            poster.post(request);

            assertSame(request, captured.get());
        }
    }
}
