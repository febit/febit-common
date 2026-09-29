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
package org.febit.lang.util;

import org.junit.jupiter.api.Test;
import org.tabletest.junit.TableTest;

import static org.junit.jupiter.api.Assertions.*;

class UriUtilsTest {

    @TableTest("""
            input    | expected
            ""       | ""
            abc      | abc
            abc/     | abc%2F
            abc/def  | abc%2Fdef
            abc/def/ | abc%2Fdef%2F
            """)
    void encode(String input, String expected) {
        assertEquals(expected, UriUtils.encode(input));
    }

    @TableTest("""
            input        | expected
            ""           | ""
            abc          | abc
            abc%2F       | abc/
            abc%2Fdef    | abc/def
            abc%2Fdef%2F | abc/def/
            """)
    void decode(String input, String expected) {
        assertEquals(expected, UriUtils.decode(input));
    }

    @TableTest("""
            base          | appends              | expected
            abc           | []                   | abc
            abc           | ["def"]              | abc/def
            abc/          | ["def", "ghi"]       | abc/def/ghi
            abc           | ["/def", "ghi"]      | abc/def/ghi
            abc/          | ["/def/", "ghi"]     | abc/def/ghi
            abc           | ["/def//", "/ghi/"]  | abc/def//ghi/
            abc           | ["/def/", "/ghi//"]  | abc/def/ghi//
            abc           | ["/def/", "/ghi///"] | abc/def/ghi///
            "https://abc" | ["/def/", "/ghi"]    | https://abc/def/ghi
            """)
    void concat(String base, String[] appends, String expected) {
        assertEquals(expected, UriUtils.concat(base, appends));
    }

    @Test
    void concat_emptySegments() {
        assertEquals("abc/", UriUtils.concat("abc", ""));
        assertEquals("abc/", UriUtils.concat("abc", "", "", ""));
    }
}
