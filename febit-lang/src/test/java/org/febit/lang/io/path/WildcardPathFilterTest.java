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
package org.febit.lang.io.path;

import org.junit.jupiter.api.Test;
import org.tabletest.junit.TableTest;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;


class WildcardPathFilterTest {

    @Test
    void create() {
        var filter = WildcardPathFilter.create(Path.of("/a/b"), "c");

        assertEquals("/a/b/", filter.baseDir);
        assertEquals("c", filter.pattern);
        assertTrue(filter.sensitive);

        filter = WildcardPathFilter.create(Path.of("/a/../b"), "", false);
        assertEquals("/b/", filter.baseDir);
        assertEquals("*", filter.pattern);
        assertFalse(filter.sensitive);

        filter = WildcardPathFilter.create(Path.of("/a/"), "/b/*.yml");
        assertEquals("/a/", filter.baseDir);
        assertEquals("b/*.yml", filter.pattern);
        assertTrue(filter.sensitive);

        assertDoesNotThrow(() -> WildcardPathFilter.create(Path.of("/.."), ""));
    }

    @TableTest("""
            pattern | sensitive | path        | expected
            *       | true      | /a/b/c      | CONTINUE
            *       | true      | /a/b/C      | CONTINUE
            *       | true      | /a/b/c/d    | CONTINUE
            *       | true      | /a/b/../b/c | CONTINUE
            *       | true      | /x/../a/b/c | CONTINUE
            *       | true      | /../a/b/c   | CONTINUE
            *       | true      | /A/B/C      | TERMINATE
            *       | true      | /a          | TERMINATE
            *       | true      | /a/b/../c   | TERMINATE
            *       | false     | /a/b/c      | CONTINUE
            *       | false     | /a/b/C      | CONTINUE
            *       | false     | /A/B/C      | TERMINATE
            *       | false     | /a          | TERMINATE
            *       | false     | /a/b/../c   | TERMINATE
            c       | true      | /a/b/c      | CONTINUE
            c       | true      | /a/b/C      | TERMINATE
            c       | true      | /a/b/d      | TERMINATE
            c       | false     | /a/b/c      | CONTINUE
            c       | false     | /a/b/C      | CONTINUE
            c       | false     | /a/b/d      | TERMINATE
            """)
    void accept(String pattern, boolean sensitive, String path, FileVisitResult expected) {
        var attrs = mock(BasicFileAttributes.class);
        var filter = WildcardPathFilter.create(Path.of("/a/b"), pattern, sensitive);
        assertEquals(expected, filter.accept(Path.of(path), attrs));
    }

    @TableTest("""
            input             | expected
            /..               | /
            /../              | /
            /../..            | /
            /../abc           | /abc
            /a/b/../../../abc | /abc
            /                 | /
            /.                | /
            /a/b/c/../../d    | /a/d
            """)
    void getAbsolutePath(String input, String expected) {
        assertEquals(expected, WildcardPathFilter.absolutePath(Path.of(input)));
    }
}
