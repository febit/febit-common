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

import static org.assertj.core.api.Assertions.assertThat;

class IQueryDaoTest {

    static class FooQueryDao extends BaseDao<TFoo, FooPO, FooRecord>
            implements IQueryDao<TFoo, FooPO, Long, FooRecord> {

        public FooQueryDao(Configuration conf) {
            super(conf);
        }
    }

    @Nested
    class FooFindById extends FooTestSupport {
        private final FooQueryDao dao = new FooQueryDao(conf());

        @Test
        void found() {
            crud().insert(foo("Alice"));
            var id = crud().requireFirst().getId();
            assertThat(dao.findById(id))
                    .isNotNull()
                    .extracting(FooPO::getName)
                    .isEqualTo("Alice");
        }

        @Test
        void notFound() {
            assertThat(dao.findById(-1L)).isNull();
        }
    }

    @Nested
    class FooListByIds extends FooTestSupport {
        private final FooQueryDao dao = new FooQueryDao(conf());

        @Test
        void singleId() {
            crud().insert(foo("Alice"));
            var id = crud().requireFirst().getId();
            assertThat(dao.listByIds(id)).hasSize(1);
        }

        @Test
        void multipleIds() {
            crud().insert(foo("A"), foo("B"), foo("C"));
            var ids = crud().listAll().stream().map(FooPO::getId).toList();
            assertThat(dao.listByIds(ids)).hasSize(3);
        }

        @Test
        void noMatch() {
            crud().insert(foo("Alice"));
            assertThat(dao.listByIds(-1L)).isEmpty();
        }
    }

    @Nested
    class FooExistsById extends FooTestSupport {
        private final FooQueryDao dao = new FooQueryDao(conf());

        @Test
        void true_() {
            crud().insert(foo("Alice"));
            var id = crud().requireFirst().getId();
            assertThat(dao.existsById(id)).isTrue();
        }

        @Test
        void false_() {
            assertThat(dao.existsById(-1L)).isFalse();
        }
    }
}
