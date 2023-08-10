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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Page<T> {

    private PaginationMeta pagination;
    private List<T> rows;

    @Nonnull
    public static <T> Page<T> of(Pagination pagination, long total, List<T> rows) {
        return of(PaginationMeta.of(pagination.getPage(), pagination.getSize(), total), rows);
    }

    @Nonnull
    public static <T> Page<T> of(@Nonnull PaginationMeta page, @Nullable List<T> rows) {
        return new Page<>(page, rows);
    }

    @Nonnull
    public static <T> Page<T> empty() {
        return Page.of(1, 0, 0, List.of());
    }

    @Nonnull
    public static <T> Page<T> of(
            int page, int size, long total, List<T> rows) {
        return of(PaginationMeta.of(page, size, total), rows);
    }

    public <D> Page<D> transfer(@Nonnull Function<T, D> action) {
        return Page.of(getPagination(),
                Lists.transfer(getRows(), action));
    }

    @Nullable
    public List<T> getRows() {
        return rows;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    public static class PaginationMeta {
        private int page;
        private int size;
        private long total;

        @JsonIgnore
        public boolean isLastPage() {
            return this.total <= (long) this.page * this.size;
        }
    }
}
