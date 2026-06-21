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

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.febit.lang.util.MapCollectors.Factories.concurrentMapFactory;
import static org.febit.lang.util.MapCollectors.Factories.hashMapFactory;
import static org.febit.lang.util.MapCollectors.Factories.linkedHashMapFactory;
import static org.febit.lang.util.MapCollectors.Factories.treeMapFactory;
import static org.junit.jupiter.api.Assertions.*;

class MapCollectorsTest {

    @Test
    void factories() {
        assertInstanceOf(HashMap.class, hashMapFactory().get());
        assertInstanceOf(LinkedHashMap.class, linkedHashMapFactory().get());
        assertInstanceOf(TreeMap.class, treeMapFactory().get());
        assertInstanceOf(TreeMap.class, treeMapFactory(Comparator.reverseOrder()).get());
        assertInstanceOf(ConcurrentHashMap.class, concurrentMapFactory().get());
    }

    @Test
    void of_parallelStream_mergesMaps() {
        // Triggers mapMerger (lines 57-61) via parallel stream combining
        var result = Stream.of("a:1", "b:2", "a:3", "b:4", "c:5")
                .parallel()
                .collect(MapCollectors.of(
                        s -> s.split(":")[0],
                        s -> s.split(":")[1],
                        (v1, v2) -> v1 + "+" + v2,
                        HashMap::new
                ));
        assertEquals(3, result.size());
        // "a" has values "1" and "3" → merged to "1+3"
        assertTrue(result.get("a").contains("+"));
        // "b" has values "2" and "4" → merged to "2+4"
        assertTrue(result.get("b").contains("+"));
        assertEquals("5", result.get("c"));
    }

    @Test
    void of_parallelStream_emptySource() {
        var result = Stream.<String>empty()
                .parallel()
                .collect(MapCollectors.of(
                        s -> s,
                        s -> s,
                        (v1, v2) -> v1,
                        HashMap::new
                ));
        assertTrue(result.isEmpty());
    }

    @Test
    void of_parallelStream_nullValueRemovesKey() {
        // value == null triggers map.remove(k) (line 50-51)
        var result = Stream.of("remove:9", "keep:5", "remove:3", "other:7")
                .parallel()
                .collect(MapCollectors.of(
                        s -> s.split(":")[0],
                        s -> {
                            var val = s.split(":")[1];
                            return "9".equals(val) || "3".equals(val) ? null : val;
                        },
                        (v1, v2) -> v2,
                        HashMap::new
                ));
        // "remove" key should be absent because value was null
        assertFalse(result.containsKey("remove"));
        assertEquals("5", result.get("keep"));
        assertEquals("7", result.get("other"));
    }

    @Test
    void of_parallelStream_overwriteMerge() {
        var result = Stream.of("k:v1", "k:v2", "k:v3")
                .parallel()
                .collect(MapCollectors.of(
                        s -> s.split(":")[0],
                        s -> s.split(":")[1],
                        (v1, v2) -> v2, // keep latest
                        HashMap::new
                ));
        assertEquals(1, result.size());
        assertNotNull(result.get("k"));
    }
}
