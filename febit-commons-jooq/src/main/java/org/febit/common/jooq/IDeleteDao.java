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

import org.febit.lang.util.Lists;
import org.jooq.Condition;
import org.jooq.UpdatableRecord;

import java.util.Collection;

import static java.util.Arrays.asList;

public interface IDeleteDao<TB extends ITable<R, ID>, PO extends IEntity<ID>, ID, R extends UpdatableRecord<R>>
        extends IDao<TB, PO, R> {

    default int delete(PO po) {
        var id = po.id();
        if (id == null) {
            return 0;
        }
        return deleteById(id);
    }

    default int deleteById(ID id) {
        return deleteBy(table().pkField().eq(id));
    }

    default int deleteBy(Condition... conditions) {
        return dsl().deleteFrom(table())
                .where(conditions)
                .execute();
    }

    default int delete(Collection<PO> pos) {
        return deleteByIds(
                Lists.collect(pos, IEntity::id)
        );
    }

    default int deleteByIds(Collection<ID> ids) {
        return deleteBy(table().pkField().in(ids));
    }

    @SuppressWarnings("unchecked")
    default int delete(PO... pos) {
        return deleteByIds(
                Lists.collect(pos, IEntity::id)
        );
    }

    @SuppressWarnings("unchecked")
    default int deleteByIds(ID... ids) {
        return deleteByIds(asList(ids));
    }

}
