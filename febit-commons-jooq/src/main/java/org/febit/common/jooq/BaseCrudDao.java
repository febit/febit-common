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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.febit.lang.util.TypeParameters;
import org.jooq.Configuration;
import org.jooq.UpdatableRecord;

import java.util.Objects;

/**
 * Base CRUD DAO impl.
 *
 * @param <R>  Record Type
 * @param <PO> Persistent Object Type
 * @param <TB> Table Type
 * @param <ID> ID Field Type
 * @see ICrudDao
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseCrudDao<TB extends ITable<R, ID>, PO extends IEntity<ID>, ID, R extends UpdatableRecord<R>>
        extends BaseDao<TB, PO, R> implements ICrudDao<TB, PO, ID, R> {

    private final Class<ID> pkType;

    @SuppressFBWarnings({
            "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"
    })
    protected BaseCrudDao(Configuration conf) {
        super(conf);
        this.pkType = TypeParameters.resolve(getClass(), BaseCrudDao.class, 2);
        Objects.requireNonNull(this.pkType);
    }

    protected Class<ID> pkType() {
        return pkType;
    }

}
