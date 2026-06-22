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

import org.febit.common.jooq.foo.FooPO;
import org.febit.common.jooq.foo.FooRecord;
import org.febit.common.jooq.foo.FooTestSupport;
import org.febit.common.jooq.foo.TFoo;
import org.jooq.Configuration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IDeleteDaoTest {

    static class FooDeleteDao extends BaseDao<TFoo, FooPO, FooRecord>
            implements IDeleteDao<TFoo, FooPO, Long, FooRecord> {

        public FooDeleteDao(Configuration conf) {
            super(conf);
        }
    }

    @Nested
    class Foo extends FooTestSupport {
        private final FooDeleteDao dao = new FooDeleteDao(conf());

        @Test
        void byPo() {
            crud().insert(foo("Alice"));
            var stored = crud().requireFirst();
            assertThat(dao.delete(stored)).isEqualTo(1);
            assertThat(crud().listAll()).isEmpty();
        }

        @Test
        void byId() {
            crud().insert(foo("Alice"));
            var id = crud().requireFirst().getId();
            assertThat(dao.deleteById(id)).isEqualTo(1);
            assertThat(crud().listAll()).isEmpty();
        }

        @Test
        void byCondition() {
            crud().insert(foo("Alice"), foo("Bob"));
            var deleted = dao.deleteBy(dao.table().NAME.eq("Alice"));
            assertThat(deleted).isEqualTo(1);
            assertThat(crud().listAll()).hasSize(1)
                    .first().extracting(FooPO::getName).isEqualTo("Bob");
        }

        @Test
        void byIds() {
            crud().insert(foo("A"), foo("B"), foo("C"));
            var all = crud().listAll();
            var ids = List.of(all.get(0).getId(), all.get(1).getId());
            assertThat(dao.deleteByIds(ids)).isEqualTo(2);
            assertThat(crud().listAll()).hasSize(1);
        }

        @Test
        void multiplePos() {
            crud().insert(foo("Alice"), foo("Bob"));
            var all = crud().listAll();
            assertThat(dao.delete(all)).isEqualTo(2);
            assertThat(crud().listAll()).isEmpty();
        }

        @Test
        void poWithNullId() {
            assertThat(dao.delete(new FooPO())).isZero();
        }

        @Test
        void byPosVarargs() {
            crud().insert(foo("Alice"), foo("Bob"));
            var all = crud().listAll();
            assertThat(dao.delete(all.get(0), all.get(1))).isEqualTo(2);
            assertThat(crud().listAll()).isEmpty();
        }

        @Test
        void byIdsVarargs() {
            crud().insert(foo("A"), foo("B"), foo("C"));
            var all = crud().listAll();
            assertThat(dao.deleteByIds(all.get(0).getId(), all.get(1).getId())).isEqualTo(2);
            assertThat(crud().listAll()).hasSize(1);
        }
    }
}
