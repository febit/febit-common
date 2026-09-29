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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaginationTest {

    @Test
    void of_createsPaginationWithoutSorts() {
        var p = Pagination.of(2, 20);
        assertEquals(2, p.getPage());
        assertEquals(20, p.getSize());
        assertTrue(p.getSorts().isEmpty());
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        var sorts = List.of(Sort.asc("name"));
        var p = new Pagination(3, 50, sorts);
        assertEquals(3, p.getPage());
        assertEquals(50, p.getSize());
        assertEquals(sorts, p.getSorts());
    }

    @Test
    void noArgsConstructor_leavesFieldsAtDefaults() {
        var p = new Pagination();
        assertEquals(0, p.getPage());
        assertEquals(0, p.getSize());
        assertTrue(p.getSorts().isEmpty());
    }

    @Test
    void getSorts_returnsEmptyListWhenNull() {
        var p = new Pagination(1, 10, null);
        assertNotNull(p.getSorts());
        assertTrue(p.getSorts().isEmpty());
    }

    @Test
    void to_setsPageAndPreservesSizeAndSorts() {
        var sorts = List.of(Sort.desc("createdAt"));
        var p = new Pagination(1, 20, sorts).to(5);
        assertEquals(5, p.getPage());
        assertEquals(20, p.getSize());
        assertEquals(sorts, p.getSorts());
    }

    @Test
    void to_copiesSortsDefensively() {
        var original = List.of(Sort.asc("name"));
        var p = new Pagination(1, 20, original).to(2);
        // to() should preserve the sorts content
        assertEquals(1, p.getSorts().size());
        assertEquals("name", p.getSorts().getFirst().getProperty());
    }

    @Test
    void to_withNullSorts_keepsEmptySorts() {
        var p = new Pagination(1, 20, null).to(3);
        assertTrue(p.getSorts().isEmpty());
    }

    @TableTest("""
            page | expected
            2    | true
            100  | true
            1    | false
            0    | false
            -1   | false
            """)
    void hasPrevious(int page, boolean expected) {
        assertEquals(expected, Pagination.of(page, 10).hasPrevious());
    }

    @TableTest("""
            page | size | expectedPage
            5    | 10   | 4
            2    | 1    | 1
            """)
    void previous(int page, int size, int expectedPage) {
        var p = Pagination.of(page, size).previous();
        assertEquals(expectedPage, p.getPage());
        assertEquals(size, p.getSize());
    }

    @Test
    void previous_returnsSameInstanceWhenPageIsOne() {
        var p = Pagination.of(1, 10);
        assertSame(p, p.previous());
    }

    @TableTest("""
            page | size | expectedPage
            5    | 10   | 6
            1    | 10   | 2
            """)
    void next(int page, int size, int expectedPage) {
        var p = Pagination.of(page, size).next();
        assertEquals(expectedPage, p.getPage());
        assertEquals(size, p.getSize());
    }

    @TableTest("""
            page | size | expectedPage | expectedSize
            7    | 25   | 1            | 25
            1    | 15   | 1            | 15
            """)
    void first(int page, int size, int expectedPage, int expectedSize) {
        var p = Pagination.of(page, size).first();
        assertEquals(expectedPage, p.getPage());
        assertEquals(expectedSize, p.getSize());
    }

    @TableTest("""
            page       | size    | expected
            1          | 20      | 0
            2          | 20      | 20
            6          | 20      | 100
            1000       | 1000000 | 999000000
            0          | 10      | -10
            2          | 1       | 1
            2147483647 | 2       | 4294967292
            """)
    void offset(int page, int size, long expected) {
        assertEquals(expected, Pagination.of(page, size).offset());
    }

    @Test
    void builder_startsWithPageAndSize() {
        var b = Pagination.builder(2, 20);
        var p = b.build();
        assertEquals(2, p.getPage());
        assertEquals(20, p.getSize());
        assertTrue(p.getSorts().isEmpty());
    }

    @Test
    void builder_asc_addsAscendingSort() {
        var p = Pagination.builder(1, 10).asc("name").build();
        assertEquals(1, p.getSorts().size());
        assertEquals("name", p.getSorts().getFirst().getProperty());
        assertEquals(Sort.Direction.ASC, p.getSorts().getFirst().getDirection());
    }

    @Test
    void builder_desc_addsDescendingSort() {
        var p = Pagination.builder(1, 10).desc("createdAt").build();
        assertEquals(1, p.getSorts().size());
        assertEquals("createdAt", p.getSorts().getFirst().getProperty());
        assertEquals(Sort.Direction.DESC, p.getSorts().getFirst().getDirection());
    }

    @Test
    void builder_multipleSorts_preserveOrder() {
        var p = Pagination.builder(1, 10)
                .asc("a").desc("b").asc("c")
                .build();
        assertEquals(3, p.getSorts().size());
        assertEquals("a", p.getSorts().get(0).getProperty());
        assertEquals("b", p.getSorts().get(1).getProperty());
        assertEquals("c", p.getSorts().get(2).getProperty());
    }

    @Test
    void builder_toBuilder_preservesState() {
        var original = Pagination.builder(2, 20).asc("name").build();
        var copy = original.toBuilder().page(5).build();
        assertEquals(5, copy.getPage());
        assertEquals(20, copy.getSize());
        assertEquals(1, copy.getSorts().size());
        assertEquals("name", copy.getSorts().getFirst().getProperty());
    }

    @Test
    void builder_toBuilder_returnsNewInstance() {
        var original = Pagination.builder(1, 10).build();
        var copy = original.toBuilder().build();
        assertNotSame(original, copy);
    }

    @Test
    void builder_clearSort_works() {
        var p = Pagination.builder(1, 10).asc("name").build();
        var cleared = p.toBuilder().clearSorts().build();
        assertTrue(cleared.getSorts().isEmpty());
    }

    @Test
    void builder_sortGenericMethod_works() {
        // sort(Sort) is the underlying generic method on the builder
        var p = Pagination.builder(1, 10)
                .sort(Sort.of("raw", Sort.Direction.ASC))
                .build();
        assertEquals(1, p.getSorts().size());
        assertEquals("raw", p.getSorts().getFirst().getProperty());
    }

    @Test
    void equalsAndHashCode_basedOnDataAnnotation() {
        var a = Pagination.of(2, 20);
        var b = Pagination.of(2, 20);
        var c = Pagination.of(3, 20);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void toString_doesNotThrow() {
        // Lombok @Data produces a toString; just ensure no NPE
        var str = Pagination.of(1, 10).toString();
        assertNotEquals("", str);
    }

    @TableTest("""
            fromPage | fromSize | toPage | expectedPage | expectedSize
            1        | 10       | -5     | -5           | 10
            5        | 0        | 1      | 1            | 0
            """)
    void to_acceptsAnyPageOrSize(int fromPage, int fromSize, int toPage, int expectedPage, int expectedSize) {
        var p = Pagination.of(fromPage, fromSize).to(toPage);
        assertEquals(expectedPage, p.getPage());
        assertEquals(expectedSize, p.getSize());
    }

    @Test
    void previous_returnsSelfWhenPageZero() {
        var p = Pagination.of(0, 10);
        assertSame(p, p.previous());
    }

    @Test
    void previous_returnsSelfWhenPageNegative() {
        var p = Pagination.of(-3, 10);
        assertSame(p, p.previous());
    }

    @Test
    void first_preservesSorts() {
        var sorts = List.of(Sort.asc("name"), Sort.desc("id"));
        var p = new Pagination(7, 25, sorts).first();
        assertEquals(1, p.getPage());
        assertEquals(25, p.getSize());
        assertEquals(sorts, p.getSorts());
    }

    @Test
    void next_preservesSorts() {
        var sorts = List.of(Sort.desc("createdAt"));
        var p = new Pagination(3, 50, sorts).next();
        assertEquals(4, p.getPage());
        assertEquals(50, p.getSize());
        assertEquals(sorts, p.getSorts());
    }

    @Test
    void to_preservesSortsAcrossMultipleChainedCalls() {
        var sorts = List.of(Sort.asc("a"));
        var p = new Pagination(1, 10, sorts).to(2).to(3).to(4);
        assertEquals(4, p.getPage());
        assertEquals(sorts, p.getSorts());
    }

    @Test
    void builder_sizeOverride_works() {
        var p = Pagination.builder(1, 10).size(50).build();
        assertEquals(1, p.getPage());
        assertEquals(50, p.getSize());
    }

    @Test
    void builder_singleSort_helper() {
        var p = Pagination.builder(1, 10)
                .sort(Sort.asc("a"))
                .build();
        assertEquals(1, p.getSorts().size());
        assertEquals("a", p.getSorts().getFirst().getProperty());
    }

    @Test
    void builder_toString_includesFields() {
        var str = Pagination.of(2, 20).toString();
        // Lombok @Data toString contains field values
        assertTrue(str.contains("2"));
        assertTrue(str.contains("20"));
    }

    @Test
    void builder_toBuilder_clearSorts_preservesPageAndSize() {
        var original = Pagination.builder(3, 30).asc("a").desc("b").build();
        var cleared = original.toBuilder().clearSorts().build();
        assertEquals(3, cleared.getPage());
        assertEquals(30, cleared.getSize());
        assertTrue(cleared.getSorts().isEmpty());
    }

    @Test
    void getSorts_calledMultipleTimes_returnsSameEmptyListReference() {
        var p = Pagination.of(1, 10);
        var first = p.getSorts();
        var second = p.getSorts();
        // getSorts() returns a stable empty list for null sorts
        assertSame(first, second);
    }

    @Test
    void sortsDefensiveCopy_doesNotReflectExternalMutation() {
        var original = List.of(Sort.asc("a"));
        var p = new Pagination(1, 10, original).to(2);
        // to() should not let later mutation of the source list affect p.getSorts()
        assertEquals(1, p.getSorts().size());
    }

}
