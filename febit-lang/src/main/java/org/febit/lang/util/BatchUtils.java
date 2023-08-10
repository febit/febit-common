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

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

@UtilityClass
public class BatchUtils {

    public static <T> void process(Iterable<T> iter, int size, Consumer<List<T>> consumer) {
        process(iter.iterator(), size, consumer);
    }

    public static <T> void process(Iterator<T> iter, int size, Consumer<List<T>> consumer) {
        List<T> batch = new ArrayList<>(size);
        while (iter.hasNext()) {
            batch.add(iter.next());
            if (batch.size() >= size) {
                consumer.accept(batch);
                batch = new ArrayList<>(size);
            }
        }
        if (!batch.isEmpty()) {
            consumer.accept(batch);
        }
    }
}
