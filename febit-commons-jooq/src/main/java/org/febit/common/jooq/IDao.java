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

import org.jooq.Record;
import org.jooq.*;

public interface IDao<TB extends Table<R>, PO, R extends TableRecord<R>> {

    Configuration conf();

    TB table();

    default DSLContext dsl() {
        return conf().dsl();
    }

    RecordMapper<R, PO> mapper();

    default <V> RecordMapper<R, V> mapper(Class<V> beanType) {
        return mapper(table().recordType(), beanType);
    }

    default <V, R1 extends Record> RecordMapper<R1, V> mapper(RecordType<R1> recordType, Class<V> beanType) {
        return conf()
                .recordMapperProvider()
                .provide(recordType, beanType);
    }

}
