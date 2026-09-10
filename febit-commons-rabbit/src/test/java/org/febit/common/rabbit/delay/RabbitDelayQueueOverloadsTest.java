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
package org.febit.common.rabbit.delay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RabbitDelayQueueOverloadsTest {

    /**
     * A real receipt — {@code DelayReceipt} is a final record that mocks unreliably, and the tests only need identity.
     */
    private static DelayReceipt receipt() {
        return new DelayReceipt("id", "rk", Instant.EPOCH, Duration.ZERO);
    }

    private RabbitDelayQueue mockWith(DelayReceipt receipt) {
        var q = mock(RabbitDelayQueue.class,
                withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
        doReturn(receipt).when(q).offer(any(DelayMessage.class));
        return q;
    }

    @Test
    void offerWithRoutingKeyBodyAndDelayDelegatesToOfferMessage() {
        var receipt = receipt();
        var q = mockWith(receipt);
        var r = q.offer("rk", "body", Duration.ofSeconds(5));
        assertThat(r).isSameAs(receipt);
        verify(q).offer(argThat(m ->
                "rk".equals(m.routingKey())
                        && "UTF-8".equals(m.contentEncoding())
                        && new String(m.content(), StandardCharsets.UTF_8).equals("body")
                        && m.deadline() != null
                        && m.deadline().isAfter(Instant.now().minusSeconds(1))
        ));
    }

    @Test
    void offerAtWithRoutingKeyHeadersBodyAndDeadlineDelegatesToOfferMessage() {
        var receipt = receipt();
        var q = mockWith(receipt);
        var deadline = Instant.parse("2026-07-20T13:00:00Z");
        var headers = Map.<String, Object>of("k", "v");
        var r = q.offerAt("rk", headers, "body", deadline);
        assertThat(r).isSameAs(receipt);
        verify(q).offer(argThat(m ->
                "rk".equals(m.routingKey())
                        && m.deadline().equals(deadline)
                        && "UTF-8".equals(m.contentEncoding())
                        && new String(m.content(), StandardCharsets.UTF_8).equals("body")
                        && headers.equals(m.headers())
        ));
    }

    @Test
    void offerWithBodyAndDelayDelegatesWithoutRoutingKey() {
        var receipt = receipt();
        var q = mockWith(receipt);
        var r = q.offer("body", Duration.ofSeconds(5));
        assertThat(r).isSameAs(receipt);
        verify(q).offer(argThat(m ->
                m.routingKey() == null
                        && "UTF-8".equals(m.contentEncoding())
                        && new String(m.content(), StandardCharsets.UTF_8).equals("body")
        ));
    }

    @Test
    void offerAtWithHeadersBodyAndDeadlineDelegatesWithoutRoutingKey() {
        var receipt = receipt();
        var q = mockWith(receipt);
        var deadline = Instant.parse("2026-07-20T13:00:00Z");
        var headers = Map.<String, Object>of("k", "v");
        var r = q.offerAt(headers, "body", deadline);
        assertThat(r).isSameAs(receipt);
        verify(q).offer(argThat(m ->
                m.routingKey() == null
                        && m.deadline().equals(deadline)
                        && headers.equals(m.headers())
        ));
    }
}
