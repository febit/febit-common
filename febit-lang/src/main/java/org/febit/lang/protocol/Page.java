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
package org.febit.lang.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.febit.lang.util.Lists;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Page<T> {

    private Meta meta;
    private List<T> rows;

    public static <T> Page<T> empty() {
        return Page.of(1, 0, 0, List.of());
    }

    public static <T> Page<T> of(Pagination pagination, long total, List<T> rows) {
        return of(Meta.of(pagination.getPage(), pagination.getSize(), total), rows);
    }

    public static <T> Page<T> of(Meta meta, List<T> rows) {
        return new Page<>(meta, rows);
    }

    public static <T> Page<T> of(int page, int size, long total, List<T> rows) {
        return of(Meta.of(page, size, total), rows);
    }

    /**
     * @deprecated use {@link #map(Function)} instead.
     */
    @Deprecated(since = "3.2.1")
    public <D> Page<D> transfer(Function<T, D> mapping) {
        return map(mapping);
    }

    /**
     * Map each row to another type.
     *
     * @param <D>     the new type
     * @param mapping the mapping function
     * @return a new Page with mapped rows
     * @since 3.2.1
     */
    public <D> Page<D> map(Function<T, D> mapping) {
        return Page.of(
                getMeta(),
                Lists.collect(getRows(), mapping)
        );
    }

    @Nullable
    public List<T> getRows() {
        return rows;
    }

    @JsonIgnore
    @SuppressWarnings("ConstantValue")
    public boolean isLastPage() {
        if (meta != null && meta.total > 0) {
            return meta.total <= (long) meta.page * meta.size;
        }
        return rows == null || rows.isEmpty();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    public static class Meta {
        private int page;
        private int size;
        private long total;
    }
}
