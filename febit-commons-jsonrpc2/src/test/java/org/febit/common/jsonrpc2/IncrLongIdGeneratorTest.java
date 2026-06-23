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

import org.febit.common.jsonrpc2.protocol.Id;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class IncrLongIdGeneratorTest {

    @Test
    void defaultStartFromOne() {
        var gen = IncrLongIdGenerator.create();
        assertEquals(Id.of(1L), gen.next());
        assertEquals(Id.of(2L), gen.next());
        assertEquals(Id.of(3L), gen.next());
    }

    @Test
    void startFromCustomValue() {
        var gen = IncrLongIdGenerator.startFrom(100L);
        assertEquals(Id.of(100L), gen.next());
        assertEquals(Id.of(101L), gen.next());
    }

    @Test
    void startFromZero() {
        var gen = IncrLongIdGenerator.startFrom(0L);
        assertEquals(Id.of(0L), gen.next());
        assertEquals(Id.of(1L), gen.next());
    }

    @Test
    void generateUniqueIds() {
        var gen = IncrLongIdGenerator.create();
        var ids = new HashSet<Id>();
        IntStream.range(0, 1000).forEach(i -> {
            assertTrue(ids.add(gen.next()), "Duplicate id at " + i);
        });
        assertEquals(1000, ids.size());
    }

    @Test
    void consecutiveValues() {
        var gen = IncrLongIdGenerator.startFrom(50L);
        for (long expected = 50; expected < 60; expected++) {
            assertEquals(expected, gen.next().value());
        }
    }
}
