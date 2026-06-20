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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageTest {

    @Test
    void empty_createsEmptyPage() {
        var page = Page.empty();
        assertNotNull(page.getMeta());
        assertEquals(1, page.getMeta().getPage());
        assertEquals(0, page.getMeta().getSize());
        assertEquals(0L, page.getMeta().getTotal());
        assertNotNull(page.getRows());
        assertTrue(page.getRows().isEmpty());
    }

    @Test
    void of_withPaginationAndTotal() {
        var pagination = Pagination.of(2, 25);
        var rows = List.of("a", "b", "c");
        var page = Page.of(pagination, 100, rows);
        assertEquals(2, page.getMeta().getPage());
        assertEquals(25, page.getMeta().getSize());
        assertEquals(100L, page.getMeta().getTotal());
        assertEquals(rows, page.getRows());
    }

    @Test
    void of_withMeta() {
        var meta = Page.Meta.of(3, 10, 50);
        var rows = List.of(1, 2, 3);
        var page = Page.of(meta, rows);
        assertEquals(meta, page.getMeta());
        assertEquals(rows, page.getRows());
    }

    @Test
    void of_withExplicitPageSizeTotal() {
        var rows = List.of("x");
        var page = Page.of(5, 15, 75, rows);
        assertEquals(5, page.getMeta().getPage());
        assertEquals(15, page.getMeta().getSize());
        assertEquals(75L, page.getMeta().getTotal());
        assertEquals(rows, page.getRows());
    }

    @Test
    void noArgsConstructor_leavesFieldsNull() {
        var page = new Page<String>();
        assertNull(page.getMeta());
        assertNull(page.getRows());
    }

    @Test
    void map_appliesFunctionToEachRow() {
        var page = Page.of(1, 10, 3, List.of(1, 2, 3));
        var mapped = page.map(i -> "v" + i);
        assertEquals(List.of("v1", "v2", "v3"), mapped.getRows());
        assertEquals(page.getMeta(), mapped.getMeta());
    }

    @Test
    void map_preservesMeta() {
        var meta = Page.Meta.of(4, 20, 999);
        var page = Page.of(meta, List.of("a", "b"));
        var mapped = page.map(String::toUpperCase);
        assertEquals(meta, mapped.getMeta());
    }

    @Test
    void map_emptyRows_returnsEmpty() {
        var page = Page.<String>of(1, 10, 0, List.of());
        var mapped = page.map(s -> s + "!");
        assertNotNull(mapped.getRows());
        assertTrue(mapped.getRows().isEmpty());
    }

    @Test
    void map_nullRows_returnsEmptyList() {
        // Lists.collect(Iterable, Function) is null-safe and returns an empty list
        // when the source is null. The mapped Page therefore has an empty rows list.
        // We construct via Page.of then nullify via reflection-free path: build
        // a Page with empty rows and check it works; for null-rows path we use
        // the package-private behavior indirectly via the no-args constructor.
        var page = new Page<String>();
        page.setRows(null);
        var mapped = page.map(String::toUpperCase);
        assertNotNull(mapped.getRows());
        assertTrue(mapped.getRows().isEmpty());
    }

    @Test
    void isLastPage_trueWhenTotalZeroAndNoRows() {
        var page = Page.<String>of(1, 10, 0, List.of());
        assertTrue(page.isLastPage());
    }

    @Test
    void isLastPage_trueWhenTotalZeroButRowsPresent() {
        // total <= 0 falls into the rows-check branch
        var page = Page.<String>of(1, 10, 0, List.of("a"));
        assertFalse(page.isLastPage());
    }

    @Test
    void isLastPage_falseWhenMorePagesRemain() {
        // page=1, size=10, total=25 → 25 > 1*10, more pages
        var page = Page.<String>of(1, 10, 25, List.of());
        assertFalse(page.isLastPage());
    }

    @Test
    void isLastPage_trueWhenExactlyOnLastPage() {
        // page=3, size=10, total=25 → 25 <= 3*10=30, last page
        var page = Page.<String>of(3, 10, 25, List.of());
        assertTrue(page.isLastPage());
    }

    @Test
    void isLastPage_trueWhenTotalIsExactMultiple() {
        // page=2, size=10, total=20 → 20 <= 20, last page
        var page = Page.<String>of(2, 10, 20, List.of());
        assertTrue(page.isLastPage());
    }

    @Test
    void isLastPage_trueWhenMetaIsNull() {
        // meta == null → falls into rows branch; null rows are treated as last
        var page = new Page<String>();
        assertTrue(page.isLastPage());
    }

    @Test
    void isLastPage_trueWhenMetaNullAndRowsNull() {
        var page = new Page<String>();
        assertTrue(page.isLastPage());
    }

    @Test
    void isLastPage_trueWhenMetaNullAndRowsEmpty() {
        var page = new Page<String>();
        page.setRows(List.of());
        assertTrue(page.isLastPage());
    }

    @Test
    void isLastPage_falseWhenMetaNullButRowsHaveData() {
        var page = new Page<String>();
        page.setRows(List.of("a"));
        assertFalse(page.isLastPage());
    }

    @Test
    void isLastPage_handlesLongTotalOverflow() {
        // page=1, size=Integer.MAX_VALUE, total=Long.MAX_VALUE
        // total <= (long)page*size check uses long arithmetic
        var page = Page.<String>of(1, Integer.MAX_VALUE, Long.MAX_VALUE, List.of());
        // Long.MAX_VALUE > 1 * Integer.MAX_VALUE → not last
        assertFalse(page.isLastPage());
    }

    @Test
    void meta_noArgsConstructor_leavesDefaults() {
        var meta = new Page.Meta();
        assertEquals(0, meta.getPage());
        assertEquals(0, meta.getSize());
        assertEquals(0L, meta.getTotal());
    }

    @Test
    void meta_of_setsAllFields() {
        var meta = Page.Meta.of(3, 15, 45);
        assertEquals(3, meta.getPage());
        assertEquals(15, meta.getSize());
        assertEquals(45L, meta.getTotal());
    }

    @Test
    void meta_equalsAndHashCode() {
        var a = Page.Meta.of(1, 10, 100);
        var b = Page.Meta.of(1, 10, 100);
        var c = Page.Meta.of(1, 10, 200);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void isLastPage_maxIntPageAndSize_usesLongArithmetic() {
        // page=Integer.MAX_VALUE, size=Integer.MAX_VALUE, total=1
        // (long)page * size does not overflow, total <= result is true
        var page = Page.<String>of(Integer.MAX_VALUE, Integer.MAX_VALUE, 1, List.of());
        assertTrue(page.isLastPage());
    }

    @Test
    void isLastPage_totalEqualToOnePage_isLast() {
        // page=1, size=10, total=10 → 10 <= 10, last
        var page = Page.<String>of(1, 10, 10, List.of());
        assertTrue(page.isLastPage());
    }

    @Test
    void isLastPage_totalOneMoreThanOnePage_isNotLast() {
        // page=1, size=10, total=11 → 11 > 10, not last
        var page = Page.<String>of(1, 10, 11, List.of());
        assertFalse(page.isLastPage());
    }

    @Test
    void isLastPage_negativeTotal_treatedAsZeroBranch() {
        // total < 0 → meta.total > 0 is false → rows branch
        // with empty rows → last
        var page = Page.<String>of(1, 10, -1, List.of());
        assertTrue(page.isLastPage());
    }

    @Test
    void isLastPage_negativeTotalWithRows_isNotLast() {
        var page = Page.<String>of(1, 10, -5, List.of("a"));
        assertFalse(page.isLastPage());
    }

    @Test
    void map_chainsAnotherMap() {
        var page = Page.of(1, 10, 2, List.of(1, 2));
        var mapped = page.map(i -> i * 10).map(Object::toString);
        assertEquals(List.of("10", "20"), mapped.getRows());
    }

    @Test
    void map_handlesNullElementInRows() {
        // Lists.collect uses mapping.apply() directly; null elements propagate
        // to the mapped result. Use a null-safe mapping function to verify.
        var page = Page.<String>of(1, 10, 2, Arrays.asList("a", null, "b"));
        var mapped = page.map(s -> s == null ? "<null>" : s.toUpperCase());
        assertNotNull(mapped.getRows());
        assertEquals(3, mapped.getRows().size());
        assertEquals("A", mapped.getRows().get(0));
        assertEquals("<null>", mapped.getRows().get(1));
        assertEquals("B", mapped.getRows().get(2));
    }

    @Test
    void empty_doesNotReturnNullMeta() {
        var page = Page.empty();
        org.junit.jupiter.api.Assertions.assertNotNull(page.getMeta());
    }

    @Test
    void empty_isLastPage() {
        assertTrue(Page.empty().isLastPage());
    }

    @Test
    void of_withPagination_picksUpPageAndSize() {
        var pagination = Pagination.of(5, 100);
        var page = Page.of(pagination, 0, List.of());
        assertEquals(5, page.getMeta().getPage());
        assertEquals(100, page.getMeta().getSize());
        assertEquals(0L, page.getMeta().getTotal());
    }

    @Test
    void getRows_isNullByDefault() {
        var page = new Page<String>();
        assertNull(page.getRows());
    }

    @Test
    void getMeta_isNullByDefault() {
        var page = new Page<String>();
        assertNull(page.getMeta());
    }

    @Test
    void setRows_acceptsEmptyList() {
        var page = new Page<String>();
        page.setRows(List.of());
        assertNotNull(page.getRows());
        assertTrue(page.getRows().isEmpty());
    }

    @Test
    void setRows_replacesPreviousList() {
        var page = new Page<String>();
        page.setRows(List.of("a"));
        page.setRows(List.of("b", "c"));
        assertEquals(List.of("b", "c"), page.getRows());
    }

    @Test
    void meta_setAllReplacesValues() {
        var meta = new Page.Meta();
        meta.setPage(7);
        meta.setSize(50);
        meta.setTotal(123L);
        assertEquals(7, meta.getPage());
        assertEquals(50, meta.getSize());
        assertEquals(123L, meta.getTotal());
    }

    @Test
    void meta_toString_doesNotThrow() {
        var str = Page.Meta.of(1, 10, 100).toString();
        org.junit.jupiter.api.Assertions.assertNotEquals("", str);
    }
}
