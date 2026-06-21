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
package org.febit.lang.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DefaultsTest {

    @Test
    void nvl_object_returnsObjectWhenNonNull() {
        assertEquals("a", Defaults.nvl("a", "b"));
    }

    @Test
    void nvl_object_returnsDefaultWhenNull() {
        assertEquals("b", Defaults.nvl(null, "b"));
    }

    @Test
    void nvl_supplier_invokesOnlyWhenNull() {
        var counter = new AtomicInteger();
        assertEquals("a", Defaults.nvl("a", () -> {
            counter.incrementAndGet();
            return "fb";
        }));
        assertEquals(0, counter.get());

        assertEquals("fb", Defaults.nvl(null, () -> {
            counter.incrementAndGet();
            return "fb";
        }));
        assertEquals(1, counter.get());
    }

    @Test
    void nvl_supplier_calledOnceWhenNull() {
        var counter = new AtomicInteger();
        Defaults.nvl(null, () -> {
            counter.incrementAndGet();
            return "x";
        });
        assertEquals(1, counter.get());
    }

    @Test
    void collapse_returnsObjectWhenNonNull() {
        assertEquals("a", Defaults.collapse("a", "b", "c"));
    }

    @Test
    void collapse_returnsFirstNonNullDefault() {
        assertEquals("b", Defaults.collapse(null, null, "b", "c"));
    }

    @Test
    void collapse_allNull_returnsNull() {
        assertNull(Defaults.collapse(null, null, null));
    }

    @Test
    void collapse_objectIsNull_noDefaults() {
        assertNull(Defaults.collapse(null, (String[]) null));
    }

    @Test
    void collapse_emptyDefaults_returnsNull() {
        assertNull(Defaults.collapse(null));
    }

    @Test
    void collapse_emptyDefaultsAndNonNullObject_returnsObject() {
        assertEquals("a", Defaults.collapse("a"));
    }

    @Test
    void collapse_firstDefaultWins() {
        assertEquals("first", Defaults.collapse(null, "first", "second"));
    }

    @Test
    void collapse_objectBeatsAnyDefault() {
        assertEquals("a", Defaults.collapse("a", "b", "c", "d"));
    }

    @Test
    void nvl_zeroIsNotNull() {
        // 0 is not null; nvl should return 0
        Integer zero = 0;
        assertEquals(zero, Defaults.nvl(zero, 99));
    }

    @Test
    void nvl_emptyStringIsNotNull() {
        assertEquals("", Defaults.nvl("", "fb"));
    }
}
