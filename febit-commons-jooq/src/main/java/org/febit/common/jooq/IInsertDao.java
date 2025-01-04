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

import org.jooq.InsertSetStep;
import org.jooq.UpdatableRecord;

import java.util.Collection;

import static java.util.Arrays.asList;

public interface IInsertDao<TB extends ITable<R, ID>, PO extends IEntity<ID>, ID, R extends UpdatableRecord<R>>
        extends IDao<TB, PO, R> {

    default InsertSetStep<R> insert() {
        return dsl().insertInto(table());
    }

    default void insert(PO po) {
        Utils.record(conf(), table(), po, false)
                .insert();
    }

    @SuppressWarnings("unchecked")
    default void insert(PO... pos) {
        insert(asList(pos));
    }

    default void insert(Collection<PO> pos) {
        if (pos.isEmpty()) {
            return;
        }
        var records = Utils.records(conf(), table(), pos, false);
        if (pos.size() == 1) {
            records.get(0).insert();
            return;
        }
        if (Utils.isNotReturnRecordToPojo(conf())) {
            dsl().batchInsert(records).execute();
            return;
        }
        for (R record : records) {
            record.insert();
        }
    }

}
