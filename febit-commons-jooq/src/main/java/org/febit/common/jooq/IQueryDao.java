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
package org.febit.common.jooq;

import org.jooq.TableRecord;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface IQueryDao<TB extends ITable<R, ID>, PO extends IEntity<ID>, ID, R extends TableRecord<R>>
        extends IBasicQueryDao<TB, PO, R> {

    @Nullable
    default PO findById(ID id) {
        return findBy(table().pkField(), id);
    }

    @SuppressWarnings("unchecked")
    default List<PO> listByIds(ID... ids) {
        return listBy(table().pkField().in(ids));
    }

    default List<PO> listByIds(Collection<ID> ids) {
        return listBy(table().pkField().in(ids));
    }

    default boolean existsById(ID id) {
        return existsBy(table().pkField().eq(id));
    }

}
