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

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class MapTest {

    record Item(int id, String name) {
    }

    @Test
    void iResponse_map_preservesAllProperties() {
        var ts = Instant.parse("2024-01-01T00:00:00Z");
        var r = Response.ok(201, "C", "m", "old");
        r.setTimestamp(ts);

        var mapped = r.map(d -> d + "!");
        assertEquals(201, mapped.getStatus());
        assertTrue(mapped.isSuccess());
        assertEquals("C", mapped.getCode());
        assertEquals("m", mapped.getMessage());
        assertEquals("old!", mapped.getData());
        assertEquals(ts, mapped.getTimestamp());
    }

    @Test
    void iResponse_map_createsNewInstance() {
        var r = IResponse.ok("d");
        var mapped = r.map(d -> d + "!");
        assertNotSame(r, mapped);
    }

    @Test
    void iResponse_map_dataNull_stillInvokesMapping() {
        var r = IResponse.<String>ok();
        var mapped = r.map(d -> "fallback");
        assertEquals("fallback", mapped.getData());
    }

    @Test
    void iResponse_map_dataNull_mappingReturningNull() {
        var r = IResponse.<String>ok();
        var mapped = r.map(d -> null);
        assertNull(mapped.getData());
    }

    @Test
    void iResponse_map_dataNull_mappingCount() {
        var counter = new AtomicInteger();
        var r = IResponse.<String>ok();
        r.map(d -> {
            counter.incrementAndGet();
            return "x";
        });
        assertEquals(1, counter.get());
    }

    @Test
    void iResponse_map_failedResponse_preservesFailed() {
        var r = IResponse.failed(500, "C", "m", "data");
        var mapped = r.map(d -> d + "!");
        assertFalse(mapped.isSuccess());
        assertTrue(mapped.isFailed());
        assertEquals(500, mapped.getStatus());
        assertEquals("C", mapped.getCode());
        assertEquals("data!", mapped.getData());
    }

    @Test
    void iResponse_map_typeChange_preservesEverything() {
        var r = IResponse.<Integer>ok(200, "C", "m", 1);
        var mapped = r.map(i -> "n=" + i);
        assertEquals("n=1", mapped.getData());
        assertEquals(200, mapped.getStatus());
    }

    @Test
    void iResponse_map_chainedCalls() {
        var r = IResponse.ok(1);
        var result = r.map(i -> i * 2).map(i -> i + 1).map(i -> "v=" + i);
        assertEquals("v=3", result.getData());
    }

    @Test
    void iResponse_map_preservesFailedWithData() {
        var r = IResponse.failed(404, "NF", "missing", "context");
        var mapped = r.map(d -> d + "!");
        assertEquals("context!", mapped.getData());
        assertEquals(404, mapped.getStatus());
        assertEquals("NF", mapped.getCode());
        assertEquals("missing", mapped.getMessage());
    }

    @Test
    void iResponse_map_failedDataNull_preservesFailed() {
        var r = IResponse.failed(503, "DOWN", "down");
        // null + "!" in Java is the string "null!"; use a null-safe mapping
        var mapped = r.map(d -> d == null ? null : d + "!");
        assertNull(mapped.getData());
        assertEquals(503, mapped.getStatus());
        assertFalse(mapped.isSuccess());
    }

    @Test
    void iResponse_map_doesNotMutateOriginal() {
        var r = Response.ok(200, "C", "m", "original");
        var mapped = r.map(d -> d + "!");
        assertEquals("original", r.getData());
        assertEquals("original!", mapped.getData());
    }

    @Test
    void response_map_returnsConcreteResponseType() {
        var r = Response.<Integer>ok(200, "C", "m", 1);
        var mapped = r.map(i -> i * 2);
        assertTrue(mapped instanceof Response);
    }

    @Test
    void response_map_preservesCodeMessageTimestamp() {
        var ts = Instant.now();
        var r = new Response<String>();
        r.setStatus(201);
        r.setSuccess(true);
        r.setCode("C");
        r.setMessage("m");
        r.setTimestamp(ts);
        r.setData("d");

        var mapped = r.map(d -> d + "!");
        assertEquals(201, mapped.getStatus());
        assertTrue(mapped.isSuccess());
        assertEquals("C", mapped.getCode());
        assertEquals("m", mapped.getMessage());
        assertEquals("d!", mapped.getData());
        assertEquals(ts, mapped.getTimestamp());
    }

    @Test
    void response_map_copiesAllViaCopyProperties() {
        var source = Response.failed(503, "DOWN", "service down", "x");
        var mapped = source.map(d -> d + "_mapped");
        assertEquals(503, mapped.getStatus());
        assertFalse(mapped.isSuccess());
        assertEquals("DOWN", mapped.getCode());
        assertEquals("service down", mapped.getMessage());
        assertEquals("x_mapped", mapped.getData());
    }

    @Test
    void response_map_createsNewInstance() {
        var r = Response.ok(200, "C", "m", "d");
        var mapped = r.map(d -> d + "!");
        assertNotSame(r, mapped);
    }

    @Test
    void response_map_dataNull_stillInvokes() {
        var r = new Response<String>();
        r.setStatus(200);
        r.setSuccess(true);
        var mapped = r.map(d -> "fb");
        assertEquals("fb", mapped.getData());
    }

    @Test
    void page_map_appliesFunctionToEachRow() {
        var page = Page.of(1, 10, 3, List.of(1, 2, 3));
        var mapped = page.map(i -> i * 2);
        assertEquals(List.of(2, 4, 6), mapped.getRows());
    }

    @Test
    void page_map_preservesMeta() {
        var meta = Page.Meta.of(5, 25, 999);
        var page = Page.of(meta, List.of("a", "b"));
        var mapped = page.map(String::toUpperCase);
        assertEquals(meta, mapped.getMeta());
    }

    @Test
    void page_map_emptyRows_yieldsEmptyRows() {
        var page = Page.<String>of(1, 10, 0, List.of());
        var mapped = page.map(s -> s + "!");
        assertNotNull(mapped.getRows());
        assertTrue(mapped.getRows().isEmpty());
    }

    @Test
    void page_map_nullRows_yieldsEmptyList() {
        var page = new Page<String>();
        page.setMeta(Page.Meta.of(1, 10, 0));
        page.setRows(null);
        var mapped = page.map(String::toUpperCase);
        assertNotNull(mapped.getRows());
        assertTrue(mapped.getRows().isEmpty());
    }

    @Test
    void page_map_chains() {
        var page = Page.of(1, 10, 3, List.of(1, 2, 3));
        var mapped = page.map(i -> i * 10).map(i -> "v" + i);
        assertEquals(List.of("v10", "v20", "v30"), mapped.getRows());
    }

    @Test
    void page_map_typeChangeToString() {
        var page = Page.of(1, 10, 3, List.of(1, 2, 3));
        var mapped = page.map(i -> "n=" + i);
        assertEquals(List.of("n=1", "n=2", "n=3"), mapped.getRows());
    }

    @Test
    void page_map_preservesTotal() {
        var page = Page.of(1, 10, 999, List.of("a", "b"));
        var mapped = page.map(String::toUpperCase);
        assertEquals(999L, mapped.getMeta().getTotal());
    }

    @Test
    void page_map_doesNotMutateOriginal() {
        var page = Page.of(1, 10, 2, List.of("a", "b"));
        var mapped = page.map(String::toUpperCase);
        assertEquals(List.of("a", "b"), page.getRows());
        assertEquals(List.of("A", "B"), mapped.getRows());
    }

    @Test
    void listResponse_mapEach_appliesToEachItem() {
        var r = new ListResponse<Item>();
        r.setStatus(200);
        r.setSuccess(true);
        r.setData(List.of(new Item(1, "a"), new Item(2, "b")));

        var mapped = r.mapEach(Item::name);
        assertEquals(List.of("a", "b"), mapped.getData());
    }

    @Test
    void listResponse_mapEach_preservesResponseStatus() {
        var r = new ListResponse<String>();
        r.setStatus(201);
        r.setCode("CUSTOM");
        r.setMessage("m");
        r.setData(List.of("a"));

        var mapped = r.mapEach(String::toUpperCase);
        assertEquals(201, mapped.getStatus());
        assertEquals("CUSTOM", mapped.getCode());
        assertEquals("m", mapped.getMessage());
        assertEquals(List.of("A"), mapped.getData());
    }

    @Test
    void listResponse_mapEach_nullData_returnsResponseWithNullData() {
        var r = new ListResponse<String>();
        r.setStatus(200);
        r.setSuccess(true);

        var mapped = r.mapEach(String::toUpperCase);
        assertNotNull(mapped);
        assertNull(mapped.getData());
    }

    @Test
    void listResponse_mapEach_emptyList_returnsEmptyList() {
        var r = new ListResponse<String>();
        r.setStatus(200);
        r.setSuccess(true);
        r.setData(List.of());

        var mapped = r.mapEach(String::toUpperCase);
        assertNotNull(mapped.getData());
        assertTrue(mapped.getData().isEmpty());
    }

    @Test
    void listResponse_mapEach_typeChange() {
        var r = new ListResponse<Integer>();
        r.setStatus(200);
        r.setSuccess(true);
        r.setData(List.of(1, 2, 3));

        var mapped = r.<String>mapEach(i -> "v" + i);
        assertEquals(List.of("v1", "v2", "v3"), mapped.getData());
    }

    @Test
    void listResponse_mapEach_chainedCalls() {
        var r = new ListResponse<Integer>();
        r.setStatus(200);
        r.setSuccess(true);
        r.setData(List.of(1, 2, 3));

        var mapped = r
                .<Integer>mapEach(i -> i * 2)
                .mapEach(i -> i + 1);
        assertEquals(List.of(3, 5, 7), mapped.getData());
    }

    @Test
    void listResponse_mapEach_doesNotMutateOriginal() {
        var r = new ListResponse<String>();
        r.setStatus(200);
        r.setSuccess(true);
        r.setData(List.of("a", "b"));

        var mapped = r.mapEach(String::toUpperCase);
        assertEquals(List.of("a", "b"), r.getData());
        assertEquals(List.of("A", "B"), mapped.getData());
    }

    @Test
    void listResponse_mapEach_singleItem() {
        var r = new ListResponse<String>();
        r.setStatus(200);
        r.setSuccess(true);
        r.setData(List.of("only"));

        var mapped = r.mapEach(String::length);
        assertEquals(List.of(4), mapped.getData());
    }

    @Test
    void pageResponse_mapEach_appliesToEachRow() {
        var r = new PageResponse<String>();
        r.setStatus(200);
        r.setSuccess(true);
        r.setData(Page.of(1, 10, 3, List.of("a", "bb", "ccc")));

        var mapped = r.mapEach(String::length);
        assertNotNull(mapped.getData());
        assertEquals(List.of(1, 2, 3), mapped.getData().getRows());
    }

    @Test
    void pageResponse_mapEach_preservesMeta() {
        var meta = Page.Meta.of(3, 15, 100);
        var r = new PageResponse<String>();
        r.setStatus(200);
        r.setSuccess(true);
        r.setData(Page.of(meta, List.of("x", "y")));

        var mapped = r.mapEach(String::toUpperCase);
        assertNotNull(mapped.getData());
        assertEquals(meta, mapped.getData().getMeta());
    }

    @Test
    void pageResponse_mapEach_nullData_returnsResponseWithNullData() {
        var r = new PageResponse<String>();
        r.setStatus(200);
        r.setSuccess(true);

        var mapped = r.mapEach(String::toUpperCase);
        assertNotNull(mapped);
        assertNull(mapped.getData());
    }

    @Test
    void pageResponse_mapEach_emptyRows_returnsEmptyRows() {
        var r = new PageResponse<String>();
        r.setStatus(200);
        r.setSuccess(true);
        r.setData(Page.of(1, 10, 0, List.of()));

        var mapped = r.mapEach(String::toUpperCase);
        assertNotNull(mapped.getData());
        assertNotNull(mapped.getData().getRows());
        assertTrue(mapped.getData().getRows().isEmpty());
    }

    @Test
    void pageResponse_mapEach_chainedCalls() {
        var r = new PageResponse<Integer>();
        r.setStatus(200);
        r.setSuccess(true);
        r.setData(Page.of(1, 10, 3, List.of(1, 2, 3)));

        var mapped = r
                .<Integer>mapEach(i -> i * 2)
                .mapEach(i -> i + 1);
        assertNotNull(mapped.getData());
        assertEquals(List.of(3, 5, 7), mapped.getData().getRows());
    }

    @Test
    void pageResponse_mapEach_typeChange() {
        var r = new PageResponse<Integer>();
        r.setStatus(200);
        r.setSuccess(true);
        r.setData(Page.of(1, 10, 3, List.of(1, 2, 3)));

        var mapped = r.<String>mapEach(i -> "v" + i);
        assertNotNull(mapped.getData());
        assertEquals(List.of("v1", "v2", "v3"), mapped.getData().getRows());
    }

    @Test
    void pageResponse_mapEach_preservesCodeMessage() {
        var r = new PageResponse<String>();
        r.setStatus(201);
        r.setCode("CUSTOM");
        r.setMessage("m");
        r.setData(Page.of(1, 10, 1, List.of("a")));

        var mapped = r.mapEach(String::toUpperCase);
        assertEquals(201, mapped.getStatus());
        assertEquals("CUSTOM", mapped.getCode());
        assertEquals("m", mapped.getMessage());
    }

    @Test
    void iResponse_mapIfPresent_dataPresent_appliesMapping() {
        var r = IResponse.ok("v");
        var mapped = r.mapIfPresent(String::toUpperCase);
        assertEquals("V", mapped.getData());
    }

    @Test
    void iResponse_mapIfPresent_dataNull_doesNotApply() {
        var counter = new AtomicInteger();
        var r = IResponse.<String>ok();
        var mapped = r.mapIfPresent(d -> {
            counter.incrementAndGet();
            return "x";
        });
        assertEquals(0, counter.get());
        assertNull(mapped.getData());
    }

    @Test
    void iResponse_mapIfPresent_dataNull_mappingReturningNull() {
        // mapIfPresent on null data returns null data (no mapping applied)
        var r = IResponse.<String>ok();
        var mapped = r.mapIfPresent(d -> "won't run");
        assertNull(mapped.getData());
    }

    @Test
    void iResponse_mapIfPresent_dataPresent_mappingReturnsNull() {
        var r = IResponse.ok("v");
        var mapped = r.mapIfPresent(d -> null);
        assertNull(mapped.getData());
    }

    @Test
    void iResponse_mapIfPresent_preservesStatus() {
        var r = IResponse.ok(201, "C", "m", "v");
        var mapped = r.mapIfPresent(String::toUpperCase);
        assertEquals(201, mapped.getStatus());
        assertEquals("C", mapped.getCode());
        assertEquals("m", mapped.getMessage());
        assertEquals("V", mapped.getData());
    }

    @Test
    void iResponse_map_unwrapsMappingResult() {
        // mapping returning the same type works
        var r = IResponse.ok(1);
        var mapped = r.map(i -> i);
        assertEquals(1, mapped.getData());
    }

    @Test
    void iResponse_map_returnsIResponseType() {
        var r = Response.ok(200, null, null, "data");
        var mapped = r.map(String::length);
        assertEquals(4, mapped.getData());
    }

    @Test
    void response_map_canBeAssignedToIResponse() {
        // The bridge: Response.map returns Response, but Response is IResponse
        var r = Response.ok(200, null, null, "data");
        var mapped = r.map(String::length);
        assertTrue(mapped.isSuccess());
        assertEquals(4, mapped.getData());
    }

    @Test
    void iResponse_map_calledOnListResponse_preservesConcrete() {
        // IListResponse's super interface IResponse<T extends @Nullable Object>=IResponse<List<T>>
        // The map default in IResponse is overridden in concrete classes
        var r = new ListResponse<String>();
        r.setStatus(200);
        r.setSuccess(true);
        r.setData(List.of("a", "b"));

        var mapped = r.mapEach(String::toUpperCase);
        // mapEach returns ListResponse<D>; data is List<String>
        assertEquals(List.of("A", "B"), mapped.getData());
    }

    @Test
    void page_map_preservesPageMetaWhenNull() {
        // edge case: page with null meta still goes through map
        var page = new Page<String>();
        page.setRows(List.of("a", "b"));
        var mapped = page.map(String::toUpperCase);
        assertEquals(List.of("A", "B"), mapped.getRows());
        assertNull(mapped.getMeta());
    }

    @Test
    void iResponse_map_dataNull_unwrapsNullSafely() {
        // mapping is given null and can return anything
        var r = IResponse.<String>ok();
        var mapped = r.map(d -> null);
        assertNull(mapped.getData());
    }

    @Test
    void iResponse_map_dataIsSpecialChar_preserved() {
        var r = IResponse.ok("\u0000\u0001special");
        var mapped = r.map(d -> d + "!");
        assertEquals("\u0000\u0001special!", mapped.getData());
    }

    @Test
    void iResponse_map_doesNotCallMappingTwice() {
        var counter = new AtomicInteger();
        var r = IResponse.ok("x");
        r.map(d -> {
            counter.incrementAndGet();
            return d + "!";
        });
        assertEquals(1, counter.get());
    }

    @Test
    void response_map_calledRepeatedly_eachCallAppliesMapping() {
        var r = Response.ok(200, null, null, "v");
        var mapped1 = r.map(d -> d + "1");
        var mapped2 = mapped1.map(d -> d + "2");
        var mapped3 = mapped2.map(d -> d + "3");
        assertEquals("v1", mapped1.getData());
        assertEquals("v12", mapped2.getData());
        assertEquals("v123", mapped3.getData());
        // Originals are unchanged
        assertEquals("v", r.getData());
    }

    @Test
    void response_map_chained_thenCleanData() {
        var r = Response.ok(200, null, null, "data");
        var mapped = r.map(d -> d + "!").cleanData();
        assertNull(mapped.getData());
        assertEquals(200, mapped.getStatus());
    }

    @Test
    void page_map_chained_thenRowNullHandling() {
        // Lists.collect(Iterable, Function) doesn't handle null elements specially;
        // a null element will pass to mapping. Use null-safe mapping.
        var page = Page.<String>of(1, 10, 2, Arrays.asList("a", null));
        var mapped = page.map(s -> s == null ? "<null>" : s.toUpperCase());
        assertEquals(List.of("A", "<null>"), mapped.getRows());
    }

    @Test
    void iResponse_map_lambdaException_propagates() {
        var r = IResponse.ok("x");
        try {
            r.map(d -> {
                throw new IllegalStateException("boom");
            });
        } catch (IllegalStateException e) {
            assertEquals("boom", e.getMessage());
            return;
        }
        throw new AssertionError("Expected IllegalStateException");
    }

    @Test
    void iResponse_map_preservesFailedStatus() {
        var r = IResponse.failed(500, "C", "m", "data");
        var mapped = r.map(d -> d);
        assertFalse(mapped.isSuccess());
        assertTrue(mapped.isFailed());
    }

    @Test
    void iResponse_map_zeroData_preserved() {
        // 0 (boxed Integer) is not null and should pass to mapping
        var r = IResponse.ok(0);
        var mapped = r.map(i -> i + 1);
        assertEquals(1, mapped.getData());
    }

    @Test
    void iResponse_map_dataIsCollection_passesToMapping() {
        var data = List.of("a", "b");
        var r = IResponse.ok(data);
        var mapped = r.map(d -> d.size());
        assertEquals(2, mapped.getData());
    }

    @Test
    void iResponse_map_isPresentBeforeAndAfter() {
        var r = IResponse.ok("x");
        assertTrue(r.isPresent());
        var mapped = r.map(d -> d + "!");
        assertTrue(mapped.isPresent());
    }

    @Test
    void iResponse_map_nullDataBefore_presentAfter() {
        var r = IResponse.<String>ok();
        assertFalse(r.isPresent());
        var mapped = r.map(d -> "fb");
        assertTrue(mapped.isPresent());
    }

    @Test
    void iResponse_map_presentBefore_emptyAfter() {
        var r = IResponse.ok("x");
        var mapped = r.map(d -> "");
        assertTrue(mapped.isPresent(), "empty string is still present");
    }

    @Test
    void iResponse_map_dataPresent_emptyAfter_returnsEmpty() {
        // isEmpty() is based on getData() == null, not on emptiness
        var r = IResponse.ok("x");
        var mapped = r.map(d -> "");
        assertFalse(mapped.isEmpty());
    }

    @Test
    void page_map_metaIsUnchanged() {
        var meta = Page.Meta.of(7, 50, 1234);
        var page = Page.of(meta, List.of("a"));
        var mapped = page.map(String::toUpperCase);
        assertSame(meta, mapped.getMeta());
    }
}
