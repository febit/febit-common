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

import org.jooq.Configuration;
import org.jooq.UpdatableRecord;

/**
 * Base CURD DAO impl.
 *
 * @param <R>  Record Type
 * @param <PO> Persistent Object Type
 * @param <TB> Table Type
 * @param <ID> ID Field Type
 * @see ICrudDao
 * @deprecated use {@link BaseCrudDao} instead.
 */
@Deprecated(since = "3.3.2")
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseCurdDao<TB extends ITable<R, ID>, PO extends IEntity<ID>, ID, R extends UpdatableRecord<R>>
        extends BaseCrudDao<TB, PO, ID, R> implements ICurdDao<TB, PO, ID, R> {

    protected BaseCurdDao(Configuration conf) {
        super(conf);
    }
}
