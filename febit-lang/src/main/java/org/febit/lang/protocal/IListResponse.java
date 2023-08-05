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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.febit.lang.util.Lists;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

@JsonDeserialize(as = ListResponse.class)
public interface IListResponse<T> extends IResponse<List<T>> {

    @Nonnull
    default <D> IListResponse<D> transferItems(@Nonnull Function<T, D> action) {
        var target = new ListResponse<D>();
        target.copyProperties(this);
        target.setData(Lists.transfer(getData(), action));
        return target;
    }
}
