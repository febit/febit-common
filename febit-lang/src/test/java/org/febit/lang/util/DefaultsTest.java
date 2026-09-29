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
import org.tabletest.junit.TableTest;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DefaultsTest {

    @TableTest("""
            object | default | expected
            a      | b       | a
                   | b       | b
            0      | 99      | 0
            ""     | fb      | ""
            """)
    void nvl(String object, String def, String expected) {
        assertEquals(expected, Defaults.nvl(object, def));
    }

    @TableTest("""
            object | defaults     | expected
            a      | b,c          | a
                   | null,b,c     | b
                   | null,null    |
                   | first,second | first
            a      | b,c,d        | a
            a      |              | a
                   |              |
            """)
    void collapse(String object, String defaults, String expected) {
        String[] defs = defaults == null
                ? new String[0]
                : Arrays.stream(defaults.split(","))
                .map(t -> "null".equals(t) ? null : t)
                .toArray(String[]::new);
        assertEquals(expected, Defaults.collapse(object, defs));
    }

    @Test
    void collapse_nullDefaultsArray_returnsNull() {
        assertNull(Defaults.collapse(null, (String[]) null));
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
}
