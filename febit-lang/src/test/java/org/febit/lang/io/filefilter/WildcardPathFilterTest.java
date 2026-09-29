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
package org.febit.lang.io.filefilter;

import org.junit.jupiter.api.Test;
import org.tabletest.junit.TableTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class WildcardPathFilterTest {

    @Test
    void create() {
        var filter = WildcardPathFilter.create(new File("/a/b"), "c");

        assertEquals("/a/b/", filter.baseDir);
        assertEquals("c", filter.pattern);
        assertTrue(filter.sensitive);

        filter = WildcardPathFilter.create(new File("/a/../b"), "", false);
        assertEquals("/b/", filter.baseDir);
        assertEquals("*", filter.pattern);
        assertFalse(filter.sensitive);

        filter = WildcardPathFilter.create(new File("/a/"), "/b/*.yml");
        assertEquals("/a/", filter.baseDir);
        assertEquals("b/*.yml", filter.pattern);
        assertTrue(filter.sensitive);

        assertThrows(IllegalArgumentException.class, () -> WildcardPathFilter.create(new File("/.."), ""));
    }

    @TableTest("""
            pattern | sensitive | path        | expected
            *       | true      | /a/b/c      | true
            *       | true      | /a/b/C      | true
            *       | true      | /a/b/c/d    | true
            *       | true      | /a/b/../b/c | true
            *       | true      | /../a/b/c   | false
            *       | true      | /A/B/C      | false
            *       | true      | /a          | false
            *       | true      | /a/b/../c   | false
            *       | false     | /a/b/c      | true
            *       | false     | /a/b/C      | true
            *       | false     | /A/B/C      | false
            *       | false     | /a          | false
            *       | false     | /a/b/../c   | false
            c       | true      | /a/b/c      | true
            c       | true      | /a/b/C      | false
            c       | true      | /a/b/d      | false
            c       | false     | /a/b/c      | true
            c       | false     | /a/b/C      | true
            c       | false     | /a/b/d      | false
            """)
    void accept(String pattern, boolean sensitive, String path, boolean expected) {
        var filter = WildcardPathFilter.create(new File("/a/b"), pattern, sensitive);
        assertEquals(expected, filter.accept(new File(path)));
    }

    @TableTest("""
            baseFile | relative | expected
            /a/b/c   | ""       | true
            /a/b     | c        | true
            /a/b/    | c        | true
            /d       | /a/b/c   | true
            /a/b/    | /c       | false
            """)
    void accept_withRelative(String baseFile, String relative, boolean expected) {
        var filter = WildcardPathFilter.create(new File("/a/b"), "*");
        assertEquals(expected, filter.accept(new File(baseFile), relative));
    }

    @Test
    void accept_nullPath() {
        var filter = WildcardPathFilter.create(new File("/a/b"), "*");
        assertFalse(filter.accept(null));
    }

    @TableTest("""
            input             | expected
            /..               |
            /../              |
            /../..            |
            /../abc           |
            /a/b/../../../abc |
            /                 | /
            /.                | /
            /a/b/c/../../d    | /a/d
            """)
    void getAbsolutePath(String input, String expected) {
        assertEquals(expected, WildcardPathFilter.getAbsolutePath(new File(input)));
    }
}
