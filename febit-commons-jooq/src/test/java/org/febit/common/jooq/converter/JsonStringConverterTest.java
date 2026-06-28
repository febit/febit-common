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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.febit.common.jooq.converter.support.Foo.F1;
import static org.febit.common.jooq.converter.support.Foo.F2000;
import static org.febit.common.jooq.converter.support.JacksonCodecSupport.CODEC;
import static org.febit.lang.jackson.JacksonUtils.toJsonString;
import static org.junit.jupiter.api.Assertions.*;

class JsonStringConverterTest {

    @Test
    void forBean() {
        var c = JsonStringConverter.forBean(Foo.class);
        assertEquals(Foo.class, c.toType());
        assertEquals(String.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertEquals(F2000, c.from(
                toJsonString(F2000)
        ));
        assertEquals(F1, c.from(c.to(F1)));
    }

    @Test
    void forBeanWithCodec() {
        var c = JsonStringConverter.forBean(CODEC, Foo.class);
        assertEquals(Foo.class, c.toType());
        assertEquals(String.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        var json = CODEC.stringify(F2000);
        assertTrue(json.contains("\"created_by\""));
        assertEquals(F2000, c.from(json));
        assertEquals(F1, c.from(c.to(F1)));
    }

    @Test
    void forBeanList() {
        var c = JsonStringConverter.forBeanList(Foo.class);
        assertEquals(List.class, c.toType());
        assertEquals(String.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertIterableEquals(List.of(), c.from("[]"));

        assertIterableEquals(List.of(F2000), c.from(
                toJsonString(List.of(F2000))
        ));
        assertIterableEquals(List.of(F1, F2000, F2000, F1),
                c.from(c.to(List.of(F1, F2000, F2000, F1)))
        );
    }

    @Test
    void forBeanListWithCodec() {
        var c = JsonStringConverter.forBeanList(CODEC, Foo.class);
        assertEquals(List.class, c.toType());
        assertEquals(String.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertIterableEquals(List.of(), c.from("[]"));
        assertIterableEquals(List.of(F2000), c.from(c.to(List.of(F2000))));
    }

    @Test
    void forBeanArray() {
        var c = JsonStringConverter.forBeanArray(Foo.class);
        assertEquals(Foo[].class, c.toType());
        assertEquals(String.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertArrayEquals(new Foo[0], c.from("[]"));

        assertArrayEquals(new Foo[]{F2000}, c.from(
                toJsonString(new Foo[]{F2000})
        ));
        assertArrayEquals(new Foo[]{F1, F2000, F2000, F1},
                c.from(c.to(new Foo[]{F1, F2000, F2000, F1}))
        );
    }

    @Test
    void forBeanArrayWithCodec() {
        var c = JsonStringConverter.forBeanArray(CODEC, Foo.class);
        assertEquals(Foo[].class, c.toType());
        assertEquals(String.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertArrayEquals(new Foo[0], c.from("[]"));
        assertArrayEquals(new Foo[]{F2000, F1},
                c.from(c.to(new Foo[]{F2000, F1}))
        );
    }

    @Test
    void forBeanMap() {
        var c = JsonStringConverter.forBeanMap(String.class, Foo.class);
        assertEquals(Map.class, c.toType());
        assertEquals(String.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertEquals(Map.<String, Foo>of(), c.from("{}"));

        assertEquals(Map.of("2000", F2000), c.from(
                toJsonString(Map.of("2000", F2000))
        ));
        assertEquals(Map.of("1", F1, "2000", F2000),
                c.from(c.to(Map.of("1", F1, "2000", F2000)))
        );
    }

    @Test
    void forBeanMapWithCodec() {
        var c = JsonStringConverter.forBeanMap(CODEC, String.class, Foo.class);
        assertEquals(Map.class, c.toType());
        assertEquals(String.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertEquals(Map.<String, Foo>of(), c.from("{}"));
        assertEquals(Map.of("a", F1, "b", F2000),
                c.from(c.to(Map.of("a", F1, "b", F2000)))
        );
    }

    @Test
    void forBeanMapSingleArg() {
        var c = JsonStringConverter.forBeanMap(Foo.class);
        assertEquals(Map.class, c.toType());
        assertEquals(String.class, c.fromType());

        assertNull(c.from(null));
        assertNull(c.to(null));

        assertEquals(Map.of(), c.from("{}"));

        assertEquals(Map.of("a", F2000), c.from(
                toJsonString(Map.of("a", F2000))
        ));
        assertEquals(Map.of("a", F1, "b", F2000, "c", F2000, "d", F1),
                c.from(c.to(Map.of("a", F1, "b", F2000, "c", F2000, "d", F1)))
        );
    }

    @Test
    void fromEmptyString() {
        var beanC = JsonStringConverter.forBean(Foo.class);
        assertNull(beanC.from(""));

        var listC = JsonStringConverter.forBeanList(Foo.class);
        assertNull(listC.from(""));

        var arrayC = JsonStringConverter.forBeanArray(Foo.class);
        assertNull(arrayC.from(""));

        var mapC = JsonStringConverter.forBeanMap(Foo.class);
        assertNull(mapC.from(""));
    }

}
