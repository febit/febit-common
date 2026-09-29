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
package org.febit.lang.protocol;

import org.junit.jupiter.api.Test;
import org.tabletest.junit.TableTest;

import org.febit.lang.Valued;

import static org.junit.jupiter.api.Assertions.*;

class SortTest {

    @TableTest("""
            property  | direction | expectedProperty | expectedDirection
            name      | ASC       | name             | ASC
            createdAt | DESC      | createdAt        | DESC
            field     | DESC      | field            | DESC
            ""        | ASC       | ""               | ASC
            """)
    void construction(String property, Sort.Direction direction, String expectedProperty, Sort.Direction expectedDirection) {
        var sort = Sort.of(property, direction);
        assertEquals(expectedProperty, sort.getProperty());
        assertEquals(expectedDirection, sort.getDirection());
    }

    @TableTest("""
            direction | isAscExpected | isDescExpected
            ASC       | true          | false
            DESC      | false         | true
            null      | true          | false
            """)
    void directionSemantics(String direction, boolean isAscExpected, boolean isDescExpected) {
        var sort = Sort.of("x", "null".equals(direction) ? null : Sort.Direction.valueOf(direction));
        assertEquals(isAscExpected, sort.isAsc());
        assertEquals(isDescExpected, sort.isDesc());
    }

    @TableTest("""
            direction | expected
            ASC       | ASC
            DESC      | DESC
            null      | ASC
            """)
    void getDirection(String direction, Sort.Direction expected) {
        var sort = Sort.of("x", "null".equals(direction) ? null : Sort.Direction.valueOf(direction));
        assertEquals(expected, sort.getDirection());
    }

    @TableTest("""
            propA | dirA | propB | dirB | expected
            name  | ASC  | name  | ASC  | true
            name  | ASC  | name  | DESC | false
            a     | ASC  | b     | ASC  | false
            x     | null | x     | null | true
            """)
    void equals(String propA, String dirA, String propB, String dirB, boolean expected) {
        var a = Sort.of(propA, "null".equals(dirA) ? null : Sort.Direction.valueOf(dirA));
        var b = Sort.of(propB, "null".equals(dirB) ? null : Sort.Direction.valueOf(dirB));
        assertEquals(expected, a.equals(b));
        if (expected) {
            assertEquals(a.hashCode(), b.hashCode());
        }
    }

    @TableTest("""
            setTo | expectedDirection | isAscExpected | isDescExpected
            DESC  | DESC              | false         | true
            null  | ASC               | true          | false
            """)
    void setDirection(String setTo, Sort.Direction expectedDirection, boolean isAscExpected, boolean isDescExpected) {
        var sort = Sort.asc("x");
        sort.setDirection("null".equals(setTo) ? null : Sort.Direction.valueOf(setTo));
        assertEquals(expectedDirection, sort.getDirection());
        assertEquals(isAscExpected, sort.isAsc());
        assertEquals(isDescExpected, sort.isDesc());
    }

    @TableTest("""
            property  | direction | expected
            name      | ASC       | name,asc
            createdAt | DESC      | createdAt,desc
            """)
    void toString_includesPropertyAndDirectionValue(String property, Sort.Direction direction, String expected) {
        var sort = direction == Sort.Direction.ASC ? Sort.asc(property) : Sort.desc(property);
        assertEquals(expected, sort.toString());
    }

    @Test
    void desc_withNullProperty() {
        var sort = Sort.desc(null);
        assertNull(sort.getProperty());
        assertEquals(Sort.Direction.DESC, sort.getDirection());
    }

    @Test
    void dataAccessors_setAndGet() {
        var sort = Sort.of("placeholder", null);
        sort.setProperty("updatedAt");
        sort.setDirection(Sort.Direction.DESC);
        assertEquals("updatedAt", sort.getProperty());
        assertEquals(Sort.Direction.DESC, sort.getDirection());
    }

    @Test
    void dataAccessors_getDirection_defaultsToAscWhenFieldIsNull() {
        var sort = Sort.of("name", null);
        assertEquals("name", sort.getProperty());
        assertEquals(Sort.Direction.ASC, sort.getDirection());
    }

    @TableTest("""
            index | expected
            0     | ASC
            1     | DESC
            """)
    void direction_enumValues(int index, Sort.Direction expected) {
        assertEquals(2, Sort.Direction.values().length);
        assertEquals(expected, Sort.Direction.values()[index]);
    }

    @TableTest("""
            direction | expected
            ASC       | asc
            DESC      | desc
            """)
    void direction_value(Sort.Direction direction, String expected) {
        assertEquals(expected, direction.getValue());
    }

    @Test
    void direction_implementsValued() {
        assertNotNull(Sort.Direction.ASC);
        assertSame(Sort.Direction.ASC.getValue(), ((Valued<?>) Sort.Direction.ASC).getValue());
    }

    @TableTest("""
            name | expected
            ASC  | ASC
            DESC | DESC
            """)
    void direction_valueOf(String name, Sort.Direction expected) {
        assertEquals(expected, Sort.Direction.valueOf(name));
    }

    @TableTest("""
            name
            UNKNOWN
            asc
            """)
    void direction_valueOf_invalidThrows(String name) {
        assertThrows(IllegalArgumentException.class, () -> Sort.Direction.valueOf(name));
    }

    @Test
    void toString_withNullDirection_throwsNpe() {
        var sort = Sort.of("prop", null);
        assertThrows(NullPointerException.class, sort::toString);
    }
}
