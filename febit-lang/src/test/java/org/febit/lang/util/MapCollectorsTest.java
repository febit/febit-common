package org.febit.lang.util;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

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

}
