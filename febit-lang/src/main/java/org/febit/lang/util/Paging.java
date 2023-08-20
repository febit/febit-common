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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.collections4.IteratorUtils;
import org.febit.lang.protocol.Page;
import org.febit.lang.protocol.Pagination;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@UtilityClass
public class Paging {

    public static <T> Iterator<T> iterator(int pageSize, Function<Pagination, Page<T>> api) {
        return new IteratorImpl<>(Pagination.of(1, pageSize), api);
    }

    public static <T> Iterable<T> iterable(int pageSize, Function<Pagination, Page<T>> api) {
        return () -> iterator(pageSize, api);
    }

    public static <T> List<T> collect(int pageSize, Function<Pagination, Page<T>> api) {
        return Lists.collect(iterator(pageSize, api));
    }

    public static <T> Stream<T> stream(int pageSize, Function<Pagination, Page<T>> api) {
        return Streams.of(
                iterable(pageSize, api)
        );
    }

    private static class IteratorImpl<T> implements Iterator<T> {

        @Nonnull
        private Iterator<T> current = IteratorUtils.emptyIterator();

        @Nonnull
        private final Function<Pagination, Page<T>> api;

        @Nullable
        private Pagination pagination;

        private IteratorImpl(Pagination start, Function<Pagination, Page<T>> api) {
            this.api = api;
            this.pagination = start;
        }

        private boolean fetchNext() {
            if (pagination == null) {
                return false;
            }
            val result = api.apply(pagination);
            pagination = result.isLastPage() ? null
                    : pagination.next();
            current = result.getRows() != null
                    ? result.getRows().iterator()
                    : Collections.emptyIterator();
            return true;
        }

        @Override
        public boolean hasNext() {
            if (current.hasNext()) {
                return true;
            }
            if (!fetchNext()) {
                return false;
            }
            return hasNext();
        }

        @Override
        public T next() {
            return current.next();
        }
    }
}
