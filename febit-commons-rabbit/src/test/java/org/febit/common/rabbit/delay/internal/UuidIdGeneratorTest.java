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
package org.febit.common.rabbit.delay.internal;

import com.fasterxml.uuid.Generators;
import org.febit.common.rabbit.delay.DelayMessage;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class UuidIdGeneratorTest {

    private final UuidIdGenerator generator = UuidIdGenerator.of(Generators.timeBasedEpochRandomGenerator());

    @Test
    void nextProducesUuidString() {
        var id = generator.next(null);
        assertThat(id).isNotNull();
        assertThatCode(() -> UUID.fromString(id)).doesNotThrowAnyException();
    }

    @Test
    void nextIsUniqueAcrossCalls() {
        assertThat(generator.next(null)).isNotEqualTo(generator.next(null));
    }

    @Test
    void version7DefaultProducesValidUuid() {
        var msg = DelayMessage.builder()
                .deadline(Instant.now())
                .build();
        assertThatCode(() -> UUID.fromString(UuidIdGenerator.V7.next(msg)))
                .doesNotThrowAnyException();
    }
}
