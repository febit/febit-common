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

import org.febit.lang.jackson.JacksonTypes;
import org.febit.lang.jackson.JacksonUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResponseTest {

    @Test
    void mapEach_appliesFunctionToEachRow() {
        var response = new PageResponse<String>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(Page.of(1, 10, 3, List.of("a", "bb", "ccc")));

        var mapped = response.mapEach(String::length);
        assertEquals(200, mapped.getStatus());
        assertTrue(mapped.isSuccess());
        assertNotNull(mapped.getData());
        assertEquals(List.of(1, 2, 3), mapped.getData().getRows());
    }

    @Test
    void mapEach_preservesMeta() {
        var meta = Page.Meta.of(2, 5, 8);
        var response = new PageResponse<String>();
        response.setData(Page.of(meta, List.of("a", "b")));
        response.setStatus(200);
        response.setSuccess(true);

        var mapped = response.mapEach(s -> s + "!");
        assertNotNull(mapped.getData());
        assertEquals(meta, mapped.getData().getMeta());
    }

    @Test
    void mapEach_nullData_returnsResponseWithNullData() {
        var response = new PageResponse<String>();
        response.setStatus(200);
        response.setSuccess(true);

        var mapped = response.mapEach(String::toUpperCase);
        assertNotNull(mapped);
        assertNull(mapped.getData());
    }

    @Test
    void mapEach_emptyRows_returnsEmptyRows() {
        var response = new PageResponse<String>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(Page.of(1, 10, 0, List.of()));

        var mapped = response.mapEach(String::toUpperCase);
        assertNotNull(mapped.getData());
        assertNotNull(mapped.getData().getRows());
        assertTrue(mapped.getData().getRows().isEmpty());
    }

    @Test
    void mapEach_copiesResponseProperties() {
        var response = new PageResponse<String>();
        response.setStatus(201);
        response.setCode("CUSTOM");
        response.setMessage("custom-message");
        response.setData(Page.of(1, 10, 1, List.of("a")));

        var mapped = response.mapEach(String::toUpperCase);
        assertEquals(201, mapped.getStatus());
        assertEquals("CUSTOM", mapped.getCode());
        assertEquals("custom-message", mapped.getMessage());
    }

    @Test
    void jsonify_roundTrips() {
        var original = new PageResponse<String>();
        original.setStatus(200);
        original.setSuccess(true);
        original.setData(Page.of(1, 10, 2, List.of("a", "b")));

        var json = JacksonUtils.toJsonString(original);
        var parsed = JacksonUtils.parse(json, PageResponse.class);
        assertEquals(original, parsed);
    }

    @Test
    void jsonify_includesMetaAndRows() {
        var response = new PageResponse<String>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(Page.of(2, 5, 8, List.of("x", "y")));

        var map = JacksonUtils.toNamedMap(response);
        assertNotNull(map);
        assertEquals(true, map.get("success"));
        assertEquals(200, map.get("status"));
        @SuppressWarnings("unchecked")
        var data = (java.util.Map<String, Object>) map.get("data");
        assertNotNull(data);
        @SuppressWarnings("unchecked")
        var meta = (java.util.Map<String, Object>) data.get("meta");
        assertEquals(2, meta.get("page"));
        assertEquals(5, meta.get("size"));
        assertEquals(8, ((Number) meta.get("total")).longValue());
        @SuppressWarnings("unchecked")
        var rows = (List<String>) data.get("rows");
        assertEquals(List.of("x", "y"), rows);
    }

    @Test
    void noArgsConstructor_empty() {
        var r = new PageResponse<String>();
        assertEquals(0, r.getStatus());
        assertFalse(r.isSuccess());
        assertNull(r.getData());
    }

    @Test
    void mapEach_preservesTimestamp() {
        var response = new PageResponse<String>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(Page.of(1, 10, 1, List.of("a")));

        var ts = response.getTimestamp();
        var mapped = response.mapEach(String::toUpperCase);
        assertEquals(ts, mapped.getTimestamp());
    }

    @Test
    void mapEach_toDifferentType() {
        var response = new PageResponse<Integer>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(Page.of(1, 10, 3, List.of(1, 2, 3)));

        var mapped = response.<String>mapEach(i -> "v" + i);
        assertNotNull(mapped.getData());
        assertEquals(List.of("v1", "v2", "v3"), mapped.getData().getRows());
    }

    @Test
    void mapEach_singleRow() {
        var response = new PageResponse<String>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(Page.of(1, 10, 1, List.of("only")));

        var mapped = response.mapEach(String::length);
        assertNotNull(mapped.getData());
        assertEquals(List.of(4), mapped.getData().getRows());
    }

    @Test
    void mapEach_chainedCalls() {
        var response = new PageResponse<Integer>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(Page.of(1, 10, 3, List.of(1, 2, 3)));

        var mapped = response
                .<Integer>mapEach(i -> i * 2)
                .mapEach(i -> i + 1);
        assertNotNull(mapped.getData());
        assertEquals(List.of(3, 5, 7), mapped.getData().getRows());
    }

    @Test
    void jsonify_emptyDataRoundTrips() {
        var original = new PageResponse<String>();
        original.setStatus(200);
        original.setSuccess(true);
        original.setData(Page.of(1, 10, 0, List.of()));

        var json = JacksonUtils.toJsonString(original);
        var parsed = JacksonUtils.parse(json, PageResponse.class);
        assertEquals(original, parsed);
    }

    @Test
    void jsonify_failedRoundTrips() {
        var original = new PageResponse<String>();
        original.setStatus(500);
        original.setSuccess(false);
        original.setCode("ERROR");
        original.setMessage("failed");
        original.setData(Page.of(1, 10, 0, List.of()));

        var parsed = JacksonUtils.parse(
                JacksonUtils.toJsonString(original),
                JacksonTypes.FACTORY.constructParametricType(PageResponse.class, String.class)
        );
        assertEquals(original, parsed);
    }

    @Test
    void jsonify_usesPageClassViaDeserialize() {
        // IPageResponse has @JsonDeserialize(as = PageResponse.class), so the
        // concrete type should be PageResponse when deserializing via the interface.
        var original = new PageResponse<String>();
        original.setStatus(200);
        original.setSuccess(true);
        original.setData(Page.of(1, 10, 1, List.of("a")));

        var json = JacksonUtils.toJsonString(original);
        @SuppressWarnings("unchecked")
        var parsed = (IPageResponse<String>) JacksonUtils.parse(
                json,
                JacksonTypes.FACTORY.constructParametricType(IPageResponse.class, String.class)
        );
        assertNotNull(parsed);
        assertNotNull(parsed.getData());
        assertEquals(200, parsed.getStatus());
        assertEquals(List.of("a"), parsed.getData().getRows());
    }

    @Test
    void mapEach_preservesTotal() {
        var response = new PageResponse<String>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(Page.of(1, 10, 999, List.of("a", "b")));

        var mapped = response.mapEach(String::toUpperCase);
        assertNotNull(mapped.getData());
        assertEquals(999L, mapped.getData().getMeta().getTotal());
    }

    @Test
    void toString_doesNotThrow() {
        var r = new PageResponse<String>();
        r.setData(Page.of(1, 10, 1, List.of("a")));
        var str = r.toString();
        assertNotNull(str);
    }
}
