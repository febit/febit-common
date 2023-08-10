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
package org.febit.lang.protocal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.List;

/**
 * Used for api clients, to match the Pagination API Spec, instead of {@code Pageable}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder(
        builderClassName = "Builder",
        toBuilder = true
)
public class Pagination {

    private int page;
    private int size;

    @Singular
    private List<Sort> sorts;

    public Pagination to(int number) {
        return new Pagination(number, size,
                sorts == null ? null : List.copyOf(sorts)
        );
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public Pagination previous() {
        return hasPrevious()
                ? to(page - 1)
                : this;
    }

    public Pagination next() {
        return to(page + 1);
    }

    public Pagination first() {
        return to(1);
    }

    public static Builder builder(int page, int size) {
        return new Builder()
                .page(page)
                .size(size);
    }

    public List<Sort> getSorts() {
        return sorts != null ? sorts : List.of();
    }

    public long offset() {
        return (long) (page - 1) * size;
    }

    public static class Builder {

        public Builder asc(String property) {
            return sort(Sort.asc(property));
        }

        public Builder desc(String property) {
            return sort(Sort.desc(property));
        }
    }

}
