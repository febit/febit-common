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

    @Test
    void accept() {
        var baseDir = new File("/a/b");
        var filter = WildcardPathFilter.create(baseDir, "*");

        assertTrue(filter.accept(new File("/a/b/c")));
        assertTrue(filter.accept(new File("/a/b/C")));
        assertTrue(filter.accept(new File("/a/b/c/d")));
        assertTrue(filter.accept(new File("/a/b/../b/c")));

        assertFalse(filter.accept(null));

        assertFalse(filter.accept(new File("/../a/b/c")));
        assertFalse(filter.accept(new File("/A/B/C")));
        assertFalse(filter.accept(new File("/a")));
        assertFalse(filter.accept(new File("/a/b/../c")));

        assertTrue(filter.accept(new File("/a/b/c"), ""));
        assertTrue(filter.accept(new File("/a/b"), "c"));
        assertTrue(filter.accept(new File("/a/b/"), "c"));
        assertTrue(filter.accept(new File("/d"), "/a/b/c"));
        assertFalse(filter.accept(new File("/a/b/"), "/c"));

        filter = WildcardPathFilter.create(baseDir, "*", false);
        assertTrue(filter.accept(new File("/a/b/c")));
        assertTrue(filter.accept(new File("/a/b/C")));
        assertFalse(filter.accept(new File("/A/B/C")));
        assertFalse(filter.accept(new File("/a")));
        assertFalse(filter.accept(new File("/a/b/../c")));

        filter = WildcardPathFilter.create(baseDir, "c");
        assertTrue(filter.accept(new File("/a/b/c")));
        assertFalse(filter.accept(new File("/a/b/C")));
        assertFalse(filter.accept(new File("/a/b/d")));

        filter = WildcardPathFilter.create(baseDir, "c", false);
        assertTrue(filter.accept(new File("/a/b/c")));
        assertTrue(filter.accept(new File("/a/b/C")));
        assertFalse(filter.accept(new File("/a/b/d")));
    }

    @Test
    void getAbsolutePath() {
        assertNull(WildcardPathFilter.getAbsolutePath(new File("/..")));
        assertNull(WildcardPathFilter.getAbsolutePath(new File("/../")));
        assertNull(WildcardPathFilter.getAbsolutePath(new File("/../..")));
        assertNull(WildcardPathFilter.getAbsolutePath(new File("/../abc")));
        assertNull(WildcardPathFilter.getAbsolutePath(new File("/a/b/../../../abc")));

        assertEquals("/", WildcardPathFilter.getAbsolutePath(new File("/")));
        assertEquals("/", WildcardPathFilter.getAbsolutePath(new File("/.")));
        assertEquals("/a/d", WildcardPathFilter.getAbsolutePath(new File("/a/b/c/../../d")));
    }
}
