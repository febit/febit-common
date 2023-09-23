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

import static org.junit.jupiter.api.Assertions.*;

class UriUtilsTest {

    @Test
    void encode() {
        assertEquals("", UriUtils.encode(""));
        assertEquals("abc", UriUtils.encode("abc"));
        assertEquals("abc%2F", UriUtils.encode("abc/"));
        assertEquals("abc%2Fdef", UriUtils.encode("abc/def"));
        assertEquals("abc%2Fdef%2F", UriUtils.encode("abc/def/"));
    }

    @Test
    void decode() {
        assertEquals("", UriUtils.decode(""));
        assertEquals("abc", UriUtils.decode("abc"));
        assertEquals("abc/", UriUtils.decode("abc%2F"));
        assertEquals("abc/def", UriUtils.decode("abc%2Fdef"));
        assertEquals("abc/def/", UriUtils.decode("abc%2Fdef%2F"));
    }

    @Test
    void concat() {
        assertEquals("abc", UriUtils.concat("abc"));
        assertEquals("abc/", UriUtils.concat("abc", ""));
        assertEquals("abc/", UriUtils.concat("abc", "", "", ""));

        assertEquals("abc/def", UriUtils.concat("abc", "def"));
        assertEquals("abc/def/ghi", UriUtils.concat("abc/", "def", "ghi"));
        assertEquals("abc/def/ghi", UriUtils.concat("abc", "/def", "ghi"));
        assertEquals("abc/def/ghi", UriUtils.concat("abc/", "/def/", "ghi"));
        assertEquals("abc/def//ghi/", UriUtils.concat("abc", "/def//", "/ghi/"));
        assertEquals("abc/def/ghi//", UriUtils.concat("abc", "/def/", "/ghi//"));
        assertEquals("abc/def/ghi///", UriUtils.concat("abc", "/def/", "/ghi///"));

        assertEquals("https://abc/def/ghi", UriUtils.concat("https://abc", "/def/", "/ghi"));
    }
}
