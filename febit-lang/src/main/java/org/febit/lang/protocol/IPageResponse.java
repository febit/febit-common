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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.annotation.Nonnull;

import java.util.function.Function;

@JsonDeserialize(as = PageResponse.class)
public interface IPageResponse<T> extends IResponse<Page<T>> {

    /**
     * @deprecated use {@link #mapEach(Function)} instead
     */
    @Deprecated(since = "3.2.1")
    @Nonnull
    default <D> PageResponse<D> transferRows(@Nonnull Function<T, D> mapping) {
        return mapEach(mapping);
    }

    /**
     * Map each row to another type.
     *
     * @param <D>     the target type
     * @param mapping the mapping function
     * @since 3.2.1
     */
    @Nonnull
    default <D> PageResponse<D> mapEach(@Nonnull Function<T, D> mapping) {
        var target = new PageResponse<D>();
        target.copyProperties(this);
        if (getData() != null) {
            target.setData(getData().map(mapping));
        }
        return target;
    }
}
