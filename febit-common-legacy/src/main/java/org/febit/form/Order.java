/**
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
package org.febit.form;

import jodd.util.ArraysUtil;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * @author zqq
 */
@Deprecated
public class Order extends ArrayList<Order.Entry> {

    public static class Entry {

        public final String field;
        public final boolean asc;

        public Entry(String field, boolean asc) {
            this.field = field;
            this.asc = asc;
        }
    }

    public void add(String field, boolean asc) {
        add(new Entry(field, asc));
    }

    public Order asc(String... fields) {
        for (String field : fields) {
            add(field, true);
        }
        return this;
    }

    public Order desc(String... fields) {
        for (String field : fields) {
            add(field, false);
        }
        return this;
    }

    public Order asc(String field) {
        add(new Entry(field, true));
        return this;
    }

    public Order desc(String field) {
        add(new Entry(field, false));
        return this;
    }

    public void forEach(BiConsumer<String, Boolean> action) {
        Objects.requireNonNull(action);
        forEach(e -> action.accept(e.field, e.asc));
    }

    public Order keep(String... whiteList) {
        if (whiteList == null || whiteList.length == 0) {
            clear();
        } else {
            removeIf(e -> !ArraysUtil.contains(whiteList, e.field));
        }
        return this;
    }

    public static Order create() {
        return new Order();
    }

}
