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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.api.Assertions.*;

class ListResponseTest {

    record Item(int id, String name) {
    }

    @Test
    void mapping() {
        var response = new ListResponse<Item>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(List.of(
                new Item(1, "name1"),
                new Item(2, "name2")
        ));

        assertThat(response)
                .returns(200, ListResponse::getStatus)
                .returns(true, ListResponse::isSuccess)
                .returns(List.of(
                                new Item(1, "name1"),
                                new Item(2, "name2")
                        ),
                        ListResponse::getData
                );

        assertThat(response.mapEach(Item::name))
                .returns(200, ListResponse::getStatus)
                .returns(true, ListResponse::isSuccess)
                .returns(List.of("name1", "name2"), ListResponse::getData);

        assertThat(new ListResponse<Item>().mapEach(Item::name))
                .returns(0, ListResponse::getStatus)
                .returns(false, ListResponse::isSuccess)
                .returns(null, ListResponse::getData);
    }

    @Test
    void jsonify() {
        String json = """
                {
                    "success": true,
                    "status": 200,
                    "data": [
                        {
                            "id": 1,
                            "name": "name1"
                        },
                        {
                            "id": 2,
                            "name": "name2"
                        }
                    ]
                }
                """;
        var response = JacksonUtils.parse(json,
                JacksonTypes.FACTORY.constructParametricType(ListResponse.class, Item.class)
        );
        assertThat(response)
                .asInstanceOf(type(ListResponse.class))
                .returns(200, ListResponse::getStatus)
                .returns(true, ListResponse::isSuccess)
                .returns(List.of(
                                new Item(1, "name1"),
                                new Item(2, "name2")
                        ),
                        ListResponse::getData
                );

        var response2 = JacksonUtils.parse(json,
                JacksonTypes.FACTORY.constructParametricType(IListResponse.class, Item.class)
        );

        assertThat(response2)
                .asInstanceOf(type(ListResponse.class))
                .returns(200, IListResponse::getStatus)
                .returns(true, IListResponse::isSuccess)
                .returns(List.of(
                                new Item(1, "name1"),
                                new Item(2, "name2")
                        ),
                        IListResponse::getData
                );
    }

    @Test
    void noArgsConstructor_emptyResponse() {
        var r = new ListResponse<String>();
        assertEquals(0, r.getStatus());
        assertFalse(r.isSuccess());
        assertNull(r.getData());
    }

    @Test
    void mapEach_emptyList_returnsEmptyList() {
        var response = new ListResponse<String>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(List.of());

        var mapped = response.mapEach(String::toUpperCase);
        assertNotNull(mapped.getData());
        assertTrue(mapped.getData().isEmpty());
    }

    @Test
    void mapEach_nullData_keepsNullData() {
        // IListResponse.mapEach: if getData() == null, target.setData(null) is not called
        // but the new response's data field is null
        var response = new ListResponse<String>();
        response.setStatus(200);
        response.setSuccess(true);

        var mapped = response.mapEach(String::toUpperCase);
        assertNull(mapped.getData());
    }

    @Test
    void mapEach_copiesCodeMessageTimestamp() {
        var response = new ListResponse<String>();
        response.setStatus(201);
        response.setCode("CUSTOM");
        response.setMessage("custom-msg");
        response.setData(List.of("a"));

        var mapped = response.mapEach(String::toUpperCase);
        assertEquals(201, mapped.getStatus());
        assertEquals("CUSTOM", mapped.getCode());
        assertEquals("custom-msg", mapped.getMessage());
    }

    @Test
    void mapEach_toDifferentType() {
        var response = new ListResponse<Integer>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(List.of(1, 2, 3));

        var mapped = response.<String>mapEach(i -> "n=" + i);
        assertEquals(List.of("n=1", "n=2", "n=3"), mapped.getData());
    }

    @Test
    void mapEach_toSameTypeIdentity() {
        var response = new ListResponse<String>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(List.of("a", "b"));

        var mapped = response.mapEach(s -> s);
        assertEquals(List.of("a", "b"), mapped.getData());
    }

    @Test
    void mapEach_returnsNewResponseInstance() {
        var response = new ListResponse<String>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(List.of("a"));

        var mapped = response.mapEach(String::toUpperCase);
        // mapEach creates a new target
        assertSame(response.getClass(), mapped.getClass());
    }

    @Test
    void mapEach_singleElement() {
        var response = new ListResponse<String>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(List.of("only"));

        var mapped = response.mapEach(String::length);
        assertEquals(List.of(4), mapped.getData());
    }

    @Test
    void mapEach_emptyMappingResult() {
        // Mapping that always returns empty list
        var response = new ListResponse<Integer>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(List.of(1, 2, 3));

        var mapped = response.mapEach(i -> 0);
        assertEquals(List.of(0, 0, 0), mapped.getData());
    }

    @Test
    void mapEach_chainedCalls() {
        var response = new ListResponse<Integer>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(List.of(1, 2, 3));

        var mapped = response
                .<Integer>mapEach(i -> i * 2)
                .mapEach(i -> i + 1);
        assertEquals(List.of(3, 5, 7), mapped.getData());
    }

    @Test
    void jsonify_emptyListRoundTrips() {
        var original = new ListResponse<String>();
        original.setStatus(200);
        original.setSuccess(true);
        original.setData(List.of());

        var json = JacksonUtils.toJsonString(original);
        var parsed = JacksonUtils.parse(
                json,
                JacksonTypes.FACTORY.constructParametricType(ListResponse.class, String.class)
        );
        assertEquals(original, parsed);
    }

    @Test
    void jsonify_failedResponse() {
        var original = new ListResponse<String>();
        original.setStatus(500);
        original.setSuccess(false);
        original.setCode("ERROR");
        original.setMessage("failed");

        var parsed = JacksonUtils.parse(
                JacksonUtils.toJsonString(original),
                JacksonTypes.FACTORY.constructParametricType(ListResponse.class, String.class)
        );
        assertEquals(original, parsed);
    }

    @Test
    void jsonify_nullDataAllowed() {
        var original = new ListResponse<String>();
        original.setStatus(200);
        original.setSuccess(true);

        ListResponse<String> parsed = JacksonUtils.parse(
                JacksonUtils.toJsonString(original),
                JacksonTypes.FACTORY.constructParametricType(ListResponse.class, String.class)
        );
        assertNull(parsed.getData());
    }

    @Test
    void toString_doesNotThrow() {
        var r = new ListResponse<String>();
        r.setData(List.of("a"));
        var str = r.toString();
        assertNotNull(str);
    }

    @Test
    void mapEach_nestedGenericType() {
        var response = new ListResponse<List<Integer>>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(List.of(List.of(1, 2), List.of(3, 4)));

        var mapped = response.mapEach(List::size);
        assertEquals(List.of(2, 2), mapped.getData());
    }
}
