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
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.febit.lang.util.Lists;

import java.util.List;
import java.util.function.Function;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Page<T> {

    private Meta meta;
    private List<T> rows;

    @Nonnull
    public static <T> Page<T> empty() {
        return Page.of(1, 0, 0, List.of());
    }

    @Nonnull
    public static <T> Page<T> of(Pagination pagination, long total, @Nullable List<T> rows) {
        return of(Meta.of(pagination.getPage(), pagination.getSize(), total), rows);
    }

    @Nonnull
    public static <T> Page<T> of(@Nonnull Meta meta, @Nullable List<T> rows) {
        return new Page<>(meta, rows);
    }

    @Nonnull
    public static <T> Page<T> of(int page, int size, long total, @Nullable List<T> rows) {
        return of(Meta.of(page, size, total), rows);
    }

    public <D> Page<D> transfer(@Nonnull Function<T, D> action) {
        return Page.of(
                getMeta(),
                Lists.transfer(getRows(), action)
        );
    }

    @Nullable
    public List<T> getRows() {
        return rows;
    }

    @JsonIgnore
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
