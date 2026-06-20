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

import org.febit.lang.Valued;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SortTest {

    @Test
    void asc_createsAscendingSort() {
        var sort = Sort.asc("name");
        assertEquals("name", sort.getProperty());
        assertEquals(Sort.Direction.ASC, sort.getDirection());
    }

    @Test
    void desc_createsDescendingSort() {
        var sort = Sort.desc("createdAt");
        assertEquals("createdAt", sort.getProperty());
        assertEquals(Sort.Direction.DESC, sort.getDirection());
    }

    @Test
    void of_setsAllFields() {
        var sort = Sort.of("field", Sort.Direction.DESC);
        assertEquals("field", sort.getProperty());
        assertEquals(Sort.Direction.DESC, sort.getDirection());
    }

    @Test
    void isAsc_trueWhenDirectionAsc() {
        assertTrue(Sort.asc("x").isAsc());
    }

    @Test
    void isAsc_falseWhenDirectionDesc() {
        assertFalse(Sort.desc("x").isAsc());
    }

    @Test
    void isAsc_trueWhenDirectionIsNull() {
        // getDirection() defaults to ASC when null
        var sort = Sort.of("x", null);
        assertTrue(sort.isAsc());
    }

    @Test
    void isDesc_trueWhenDirectionDesc() {
        assertTrue(Sort.desc("x").isDesc());
    }

    @Test
    void isDesc_falseWhenDirectionAsc() {
        assertFalse(Sort.asc("x").isDesc());
    }

    @Test
    void isDesc_falseWhenDirectionIsNull() {
        var sort = Sort.of("x", null);
        assertFalse(sort.isDesc());
    }

    @Test
    void isAsc_andIsDesc_areMutuallyExclusiveForAsc() {
        var sort = Sort.asc("x");
        assertTrue(sort.isAsc());
        assertFalse(sort.isDesc());
    }

    @Test
    void isAsc_andIsDesc_areMutuallyExclusiveForDesc() {
        var sort = Sort.desc("x");
        assertFalse(sort.isAsc());
        assertTrue(sort.isDesc());
    }

    @Test
    void getDirection_returnsDefaultAscWhenFieldIsNull() {
        // Field is null but the @Data-generated getter still applies the ASC default
        var sort = Sort.of("x", null);
        assertEquals(Sort.Direction.ASC, sort.getDirection());
    }

    @Test
    void getDirection_returnsFieldValueWhenNotNull() {
        var sort = Sort.of("x", Sort.Direction.DESC);
        assertEquals(Sort.Direction.DESC, sort.getDirection());
    }

    @Test
    void toString_includesPropertyAndDirectionValue() {
        assertEquals("name,asc", Sort.asc("name").toString());
        assertEquals("createdAt,desc", Sort.desc("createdAt").toString());
    }

    @Test
    void equalsAndHashCode_basedOnDataAnnotation() {
        var a = Sort.asc("name");
        var b = Sort.asc("name");
        var c = Sort.desc("name");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void dataAccessors_setAndGet() {
        // Sort has no no-args constructor (Lombok @AllArgsConstructor with static factory).
        // Use the factory then mutate via setters.
        var sort = Sort.of("placeholder", null);
        sort.setProperty("updatedAt");
        sort.setDirection(Sort.Direction.DESC);
        assertEquals("updatedAt", sort.getProperty());
        assertEquals(Sort.Direction.DESC, sort.getDirection());
    }

    @Test
    void dataAccessors_getDirection_defaultsToAscWhenFieldIsNull() {
        // getDirection() is overridden to return ASC when the underlying field is null.
        // The raw field can be inspected via reflection if needed, but the public
        // contract via getDirection() is "never null".
        var sort = Sort.of("name", null);
        assertEquals("name", sort.getProperty());
        assertEquals(Sort.Direction.ASC, sort.getDirection());
    }

    @Test
    void direction_enumValues() {
        var values = Sort.Direction.values();
        assertEquals(2, values.length);
        assertEquals(Sort.Direction.ASC, values[0]);
        assertEquals(Sort.Direction.DESC, values[1]);
    }

    @Test
    void direction_ascValue() {
        assertEquals("asc", Sort.Direction.ASC.getValue());
    }

    @Test
    void direction_descValue() {
        assertEquals("desc", Sort.Direction.DESC.getValue());
    }

    @Test
    void direction_implementsValued() {
        assertNotNull(Sort.Direction.ASC);
        assertSame(Sort.Direction.ASC.getValue(), ((Valued<?>) Sort.Direction.ASC).getValue());
    }

    @Test
    void direction_valueOf() {
        assertEquals(Sort.Direction.ASC, Sort.Direction.valueOf("ASC"));
        assertEquals(Sort.Direction.DESC, Sort.Direction.valueOf("DESC"));
    }

    @Test
    void direction_valueOf_unknownThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> Sort.Direction.valueOf("UNKNOWN"));
    }

    @Test
    void direction_valueOf_caseSensitive() {
        assertThrows(IllegalArgumentException.class,
                () -> Sort.Direction.valueOf("asc"));
    }

    @Test
    void asc_withEmptyProperty() {
        var sort = Sort.asc("");
        assertEquals("", sort.getProperty());
        assertEquals(Sort.Direction.ASC, sort.getDirection());
    }

    @Test
    void desc_withNullProperty() {
        var sort = Sort.desc(null);
        assertNull(sort.getProperty());
        assertEquals(Sort.Direction.DESC, sort.getDirection());
    }

    @Test
    void toString_withNullDirection_throwsNpe() {
        // toString() is not null-safe for direction
        var sort = Sort.of("prop", null);
        assertThrows(NullPointerException.class, sort::toString);
    }

    @Test
    void equals_differsByProperty() {
        assertNotEquals(Sort.asc("a"), Sort.asc("b"));
    }

    @Test
    void equals_differsByDirection() {
        assertNotEquals(Sort.asc("x"), Sort.desc("x"));
    }

    @Test
    void equals_bothNullDirectionEqual() {
        // Two Sort objects both with null direction: equals depends on field equality
        var a = Sort.of("x", null);
        var b = Sort.of("x", null);
        assertEquals(a, b);
    }

    @Test
    void isAsc_returnsTrueForNullDirection() {
        // isAsc uses getDirection() which defaults to ASC when direction is null
        var sort = Sort.of("x", null);
        assertTrue(sort.isAsc());
        assertFalse(sort.isDesc());
    }

    @Test
    void setDirection_overridesValue() {
        var sort = Sort.asc("x");
        sort.setDirection(Sort.Direction.DESC);
        assertEquals(Sort.Direction.DESC, sort.getDirection());
        assertTrue(sort.isDesc());
    }

    @Test
    void setDirection_nullAllowed_getDirectionReturnsDefault() {
        var sort = Sort.asc("x");
        sort.setDirection(null);
        // getDirection() is overridden to return ASC when direction is null,
        // so it never returns null from the public API
        assertEquals(Sort.Direction.ASC, sort.getDirection());
        assertTrue(sort.isAsc());
        assertFalse(sort.isDesc());
    }
}
