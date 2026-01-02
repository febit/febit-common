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
