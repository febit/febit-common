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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DelayMessageTest {

    @Test
    void defaultsContentToEmptyArray() {
        var msg = DelayMessage.builder()
                .deadline(Instant.now())
                .build();

        assertThat(msg.content()).isEmpty();
        assertThat(msg.headers()).isEmpty();
    }

    @Test
    void jsonContentSetsTypeAndEncoding() {
        var msg = DelayMessage.builder()
                .deadline(Instant.now())
                .jsonContent("{}")
                .build();

        assertThat(msg.content()).isEqualTo("{}".getBytes(StandardCharsets.UTF_8));
        assertThat(msg.contentType()).isEqualTo("application/json");
        assertThat(msg.contentEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
    }

    @Test
    void contentSetsEncodingOnly() {
        var msg = DelayMessage.builder()
                .deadline(Instant.now())
                .content("x")
                .build();

        assertThat(msg.content()).isEqualTo("x".getBytes(StandardCharsets.UTF_8));
        assertThat(msg.contentEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        assertThat(msg.contentType()).isNull();
    }

    @Test
    void delayComputesDeadlineFromNow() {
        var before = Instant.now();
        var msg = DelayMessage.builder()
                .delay(Duration.ofSeconds(5))
                .build();

        assertThat(msg.deadline()).isBetween(before, before.plusSeconds(6));
    }

    @Test
    void accumulatesHeaders() {
        var msg = DelayMessage.builder()
                .deadline(Instant.now())
                .header("k", "v")
                .header("n", 1)
                .build();

        assertThat(msg.headers())
                .containsEntry("k", "v")
                .containsEntry("n", 1);
    }
}
