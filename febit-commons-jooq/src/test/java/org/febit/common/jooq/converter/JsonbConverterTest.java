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
package org.febit.common.jooq.converter;

import org.febit.common.jooq.converter.support.Foo;
import org.jooq.JSONB;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.febit.common.jooq.converter.support.Foo.F1;
import static org.febit.common.jooq.converter.support.Foo.F2000;
import static org.febit.common.jooq.converter.support.JacksonCodecSupport.CODEC;
import static org.febit.lang.jackson.JacksonUtils.toJsonString;
import static org.junit.jupiter.api.Assertions.*;

class JsonbConverterTest {

    @Test
    void forBean() {
        var c = JsonbConverter.forBean(Foo.class);
        assertEquals(Foo.class, c.toType());
        assertEquals(JSONB.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertEquals(F2000, c.from(JSONB.valueOf(
                toJsonString(F2000)
        )));
        assertEquals(F1, c.from(c.to(F1)));
    }

    @Test
    void forBeanWithCodec() {
        var c = JsonbConverter.forBean(CODEC, Foo.class);
        assertEquals(Foo.class, c.toType());
        assertEquals(JSONB.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        var json = CODEC.stringify(F2000);
        assertTrue(json.contains("\"created_by\""));
        assertEquals(F2000, c.from(JSONB.valueOf(json)));
        assertEquals(F1, c.from(c.to(F1)));
    }

    @Test
    void forBeanList() {
        var c = JsonbConverter.forBeanList(Foo.class);
        assertEquals(List.class, c.toType());
        assertEquals(JSONB.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertIterableEquals(List.of(), c.from(JSONB.valueOf("[]")));

        assertIterableEquals(List.of(F2000), c.from(JSONB.valueOf(
                toJsonString(List.of(F2000))
        )));
        assertIterableEquals(List.of(F1, F2000, F2000, F1),
                c.from(c.to(List.of(F1, F2000, F2000, F1)))
        );
    }

    @Test
    void forBeanListWithCodec() {
        var c = JsonbConverter.forBeanList(CODEC, Foo.class);
        assertEquals(List.class, c.toType());
        assertEquals(JSONB.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertIterableEquals(List.of(), c.from(JSONB.valueOf("[]")));
        assertIterableEquals(List.of(F2000), c.from(c.to(List.of(F2000))));
    }

    @Test
    void forBeanArray() {
        var c = JsonbConverter.forBeanArray(Foo.class);
        assertEquals(Foo[].class, c.toType());
        assertEquals(JSONB.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertArrayEquals(new Foo[0], c.from(JSONB.valueOf("[]")));

        assertArrayEquals(new Foo[]{F2000}, c.from(JSONB.valueOf(
                toJsonString(new Foo[]{F2000})
        )));
        assertArrayEquals(new Foo[]{F1, F2000, F2000, F1},
                c.from(c.to(new Foo[]{F1, F2000, F2000, F1}))
        );
    }

    @Test
    void forBeanArrayWithCodec() {
        var c = JsonbConverter.forBeanArray(CODEC, Foo.class);
        assertEquals(Foo[].class, c.toType());
        assertEquals(JSONB.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertArrayEquals(new Foo[0], c.from(JSONB.valueOf("[]")));
        assertArrayEquals(new Foo[]{F2000, F1},
                c.from(c.to(new Foo[]{F2000, F1}))
        );
    }

    @Test
    void forBeanMap() {
        var c = JsonbConverter.forBeanMap(String.class, Foo.class);
        assertEquals(Map.class, c.toType());
        assertEquals(JSONB.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertEquals(Map.<String, Foo>of(), c.from(JSONB.valueOf("{}")));

        assertEquals(Map.of("2000", F2000), c.from(JSONB.valueOf(
                toJsonString(Map.of("2000", F2000))
        )));
        assertEquals(Map.of("1", F1, "2000", F2000),
                c.from(c.to(Map.of("1", F1, "2000", F2000)))
        );
    }

    @Test
    void forBeanMapWithCodec() {
        var c = JsonbConverter.forBeanMap(CODEC, String.class, Foo.class);
        assertEquals(Map.class, c.toType());
        assertEquals(JSONB.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertEquals(Map.<String, Foo>of(), c.from(JSONB.valueOf("{}")));
        assertEquals(Map.of("a", F1, "b", F2000),
                c.from(c.to(Map.of("a", F1, "b", F2000)))
        );
    }

    @Test
    void forBeanMapSingleArg() {
        var c = JsonbConverter.forBeanMap(Foo.class);
        assertEquals(Map.class, c.toType());
        assertEquals(JSONB.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertEquals(Map.<String, Foo>of(), c.from(JSONB.valueOf("{}")));

        assertEquals(Map.of("key", F2000), c.from(JSONB.valueOf(
                toJsonString(Map.of("key", F2000))
        )));
        assertEquals(Map.of("x", F1, "y", F2000),
                c.from(c.to(Map.of("x", F1, "y", F2000)))
        );
    }

}
