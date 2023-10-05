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
package org.febit.lang.modeler;

import org.febit.lang.util.TimeUtils;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ModelerComplexTest {

    static final Schema S_COMPLEX = Schema.parseStruct(
            "demo",
            "int id\n",
            "string name\n",
            "list<string> strings #comment test\n",
            "map<string,long> longMap\n",
            "optional<map<string,string>> optionalStringMap\n",
            "struct<id:string,launch:long,du:long,date:int> session\n",
            "list<struct<" +
                    "du:long," +
                    "name:string," +
                    "ts:optional<long>,\n" +
                    "attrs:map<string,string>," +
                    "struct:struct<xx:string>," +
                    "flag:boolean" +
                    ">> events\n",
            "list<struct<time:time,date:date,dt:datetime,dtz:datetimetz,instant:instant>> times"
    );

    private static List<Object> list(Object... items) {
        return Arrays.asList(items);
    }

    private static Map<Object, Object> map(Object... items) {
        var map = new HashMap<>();
        int i = 0;
        while (i < items.length - 1) {
            map.put(items[i++], items[i++]);
        }
        return map;
    }

    @Test
    void testConvertComplex() {
        var time = ZonedDateTime.parse("2022-01-23T02:03:56+07:00");

        @SuppressWarnings("unchecked")
        var record = (List<Object>) Modeler.builder()
                .emptyStrictIfAbsent()
                .structAsList()
                .process(S_COMPLEX, map(
                        "id", 1234L,
                        "name", "Mr.X",
                        "strings", list(1, 2D, "345", 'a'),
                        "longMap", map(1, 1, "2", 2L, "NULL", null),
                        "optionalStringMap", map(1, 1, "2", 2L, "NULL", null),
                        "session", map("id", 1, "du", 2L),
                        "events", list(
                                map("name", 1, "du", 2L, "ts", 3, "flag", true),
                                map("name", "2", "du", 2L, "attrs", map(1, 1, "2", 2L, "NULL", null)),
                                map("struct", map("xx", "yy"))
                        ),
                        "times", list(
                                map(),
                                map(
                                        "time", time.toLocalTime().toString(),
                                        "date", time.toLocalDate(),
                                        "dt", time.toLocalDateTime(),
                                        "instant", time,
                                        "dtz", time.toString()
                                )
                        ),
                        "unused", "unused2"
                ));

        assertEquals(list(
                // 0 id
                1234,
                // 1 name
                "Mr.X",
                // 2 strings
                list("1", "2.0", "345", "a"),
                // 3 longMap
                map("1", 1L, "2", 2L, "NULL", 0L),
                // 4 optionalStringMap
                map("1", "1", "2", "2", "NULL", ""),
                // 5 session struct<id:string,launch:bigint,du:long,date:int>
                list("1", 0L, 2L, 0),
                // 6 events array<struct< du: bigint, name : string  ,  ts:optional<bigint>, attrs:map<String> , struct:struct<xx:String>, flag:boolean>  >
                list(list(
                                // event.du
                                2L,
                                // event.name
                                "1",
                                // event.ts
                                3L,
                                // event.attrs
                                map(),
                                // event.struct
                                list(""),
                                // event.flag
                                true
                        ), Arrays.asList(
                                // event.du
                                2L,
                                // event.name
                                "2",
                                // event.ts
                                null,
                                // event.attrs
                                map("1", "1", "2", "2", "NULL", ""),
                                // event.struct
                                list(""),
                                // event.flag
                                false
                        ), Arrays.asList(
                                // event.du
                                0L,
                                // event.name
                                "",
                                // event.ts
                                null,
                                // event.attrs
                                map(),
                                // event.struct
                                list("yy"),
                                // event.flag
                                false
                        )
                ),
                list(
                        list(
                                TimeUtils.TIME_DEFAULT,
                                TimeUtils.DATE_DEFAULT,
                                TimeUtils.DATETIME_DEFAULT,
                                TimeUtils.ZONED_DATETIME_DEFAULT,
                                TimeUtils.INSTANT_DEFAULT
                        ),
                        list(
                                time.toLocalTime(),
                                time.toLocalDate(),
                                time.toLocalDateTime(),
                                time,
                                time.toInstant()
                        )
                )
        ), record);
    }

}
