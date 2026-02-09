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

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;
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

    @Test
    void accept() {
        var baseDir = Path.of("/a/b");
        var filter = WildcardPathFilter.create(baseDir, "*");
        var attrs = mock(BasicFileAttributes.class);

        assertEquals(CONTINUE, filter.accept(Path.of("/a/b/c"), attrs));
        assertEquals(CONTINUE, filter.accept(Path.of("/a/b/C"), attrs));
        assertEquals(CONTINUE, filter.accept(Path.of("/a/b/c/d"), attrs));
        assertEquals(CONTINUE, filter.accept(Path.of("/a/b/../b/c"), attrs));
        assertEquals(CONTINUE, filter.accept(Path.of("/x/../a/b/c"), attrs));

        // NOTICE: overflowed path is still accepted, because it can be normalized by caller.
        assertEquals(CONTINUE, filter.accept(Path.of("/../a/b/c"), attrs));

        assertEquals(TERMINATE, filter.accept(Path.of("/A/B/C"), attrs));
        assertEquals(TERMINATE, filter.accept(Path.of("/a"), attrs));
        assertEquals(TERMINATE, filter.accept(Path.of("/a/b/../c"), attrs));

        filter = WildcardPathFilter.create(baseDir, "*", false);
        assertEquals(CONTINUE, filter.accept(Path.of("/a/b/c"), attrs));
        assertEquals(CONTINUE, filter.accept(Path.of("/a/b/C"), attrs));
        assertEquals(TERMINATE, filter.accept(Path.of("/A/B/C"), attrs));
        assertEquals(TERMINATE, filter.accept(Path.of("/a"), attrs));
        assertEquals(TERMINATE, filter.accept(Path.of("/a/b/../c"), attrs));

        filter = WildcardPathFilter.create(baseDir, "c");
        assertEquals(CONTINUE, filter.accept(Path.of("/a/b/c"), attrs));
        assertEquals(TERMINATE, filter.accept(Path.of("/a/b/C"), attrs));
        assertEquals(TERMINATE, filter.accept(Path.of("/a/b/d"), attrs));

        filter = WildcardPathFilter.create(baseDir, "c", false);
        assertEquals(CONTINUE, filter.accept(Path.of("/a/b/c"), attrs));
        assertEquals(CONTINUE, filter.accept(Path.of("/a/b/C"), attrs));
        assertEquals(TERMINATE, filter.accept(Path.of("/a/b/d"), attrs));
    }

    @Test
    void getAbsolutePath() {
        assertEquals("/", WildcardPathFilter.absolutePath(Path.of("/..")));
        assertEquals("/", WildcardPathFilter.absolutePath(Path.of("/../")));
        assertEquals("/", WildcardPathFilter.absolutePath(Path.of("/../..")));
        assertEquals("/abc", WildcardPathFilter.absolutePath(Path.of("/../abc")));
        assertEquals("/abc", WildcardPathFilter.absolutePath(Path.of("/a/b/../../../abc")));

        assertEquals("/", WildcardPathFilter.absolutePath(Path.of("/")));
        assertEquals("/", WildcardPathFilter.absolutePath(Path.of("/.")));
        assertEquals("/a/d", WildcardPathFilter.absolutePath(Path.of("/a/b/c/../../d")));
    }
}
