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
package org.febit.common.jooq.foo;

import org.febit.common.jooq.BaseCrudDao;
import org.jooq.Configuration;

import java.util.Objects;

public class FooDao extends BaseCrudDao<TFoo, FooPO, Long, FooRecord> {

    public FooDao(Configuration conf) {
        super(conf);
    }

    public FooPO requireFirst() {
        var po = dsl().selectFrom(T)
                .orderBy(T.ID.asc())
                .limit(1)
                .fetchOne(mapper());
        Objects.requireNonNull(po, "No record found");
        return po;
    }
}
