package org.febit.lang.io.filefilter;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class WildcardPathFilterTest {

    @Test
    void create() {
        var filter = WildcardPathFilter.create(new File("/a/b"), "c");

        assertEquals(filter.baseDir, "/a/b/");
        assertEquals(filter.pattern, "c");
        assertTrue(filter.sensitive);

        filter = WildcardPathFilter.create(new File("/a/../b"), "", false);
        assertEquals(filter.baseDir, "/b/");
        assertEquals(filter.pattern, "*");
        assertFalse(filter.sensitive);

        filter = WildcardPathFilter.create(new File("/a/"), "/b/*.yml");
        assertEquals(filter.baseDir, "/a/");
        assertEquals(filter.pattern, "b/*.yml");
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
