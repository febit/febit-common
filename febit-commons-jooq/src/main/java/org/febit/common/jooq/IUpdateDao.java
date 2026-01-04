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

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.UpdatableRecord;
import org.jooq.UpdateSetFirstStep;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;

public interface IUpdateDao<TB extends ITable<R, ID>, PO extends IEntity<ID>, ID, R extends UpdatableRecord<R>>
        extends IDao<TB, PO, R> {

    default int update(PO po) {
        return Utils.record(conf(), table(), po, true)
                .update();
    }

    @SuppressWarnings("unchecked")
    default int update(PO... objects) {
        return update(asList(objects));
    }

    default int update(Collection<PO> pos) {
        if (pos.isEmpty()) {
            return 0;
        }
        var records = Utils.records(conf(), table(), pos, true);
        if (pos.size() == 1) {
            return records.get(0).update();
        }
        if (Utils.isNotReturnRecordToPojo(conf())
                || Utils.isNotReturnAllOnUpdatableRecord(conf())) {
            var changes = dsl().batchUpdate(records).execute();
            return IntStream.of(changes).sum();
        }
        var changed = 0;
        for (R record : records) {
            changed += record.update();
        }
        return changed;
    }

    default <V> int updateFieldBy(Field<V> field, @Nullable V value, Condition... conditions) {
        return update()
                .set(field, value)
                .where(conditions)
                .execute();
    }

    default UpdateSetFirstStep<R> update() {
        return dsl().update(table());
    }

}
