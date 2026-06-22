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

class IUpdateDaoTest {

    static class FooUpdateDao extends BaseDao<TFoo, FooPO, FooRecord>
            implements IUpdateDao<TFoo, FooPO, Long, FooRecord> {

        public FooUpdateDao(Configuration conf) {
            super(conf);
        }
    }

    @Nested
    class Foo extends FooTestSupport {
        private final FooUpdateDao dao = new FooUpdateDao(conf());

        @Test
        void single() {
            crud().insert(foo("Alice"));
            var stored = crud().requireFirst();
            stored.setName("Alice-Updated");
            stored.setEnabled(false);
            dao.update(stored);

            var result = crud().requireFirst();
            assertThat(result.getName()).isEqualTo("Alice-Updated");
            assertThat(result.getEnabled()).isFalse();
        }

        @Test
        void multiple() {
            crud().insert(foo("Alice"), foo("Bob"));
            var all = crud().listAll();
            all.forEach(p -> p.setDescription("all-updated"));
            var changed = dao.update(all);
            assertThat(changed).isEqualTo(2);

            var descriptions = crud().listFieldBy(dao.table().DESCRIPTION);
            assertThat(descriptions).containsOnly("all-updated");
        }

        @Test
        void multipleVarargs() {
            crud().insert(foo("Alice"), foo("Bob"));
            var all = crud().listAll();
            all.forEach(p -> p.setDescription("varargs-updated"));
            var changed = dao.update(all.get(0), all.get(1));
            assertThat(changed).isEqualTo(2);

            var descriptions = crud().listFieldBy(dao.table().DESCRIPTION);
            assertThat(descriptions).containsOnly("varargs-updated");
        }

        @Test
        void multiple_emptyCollection() {
            crud().insert(foo("Alice"));
            assertThat(dao.update(List.of())).isZero();
            assertThat(crud().listAll()).hasSize(1);
        }

        @Test
        void single_noMatch() {
            var po = new FooPO();
            po.setId(-1L);
            po.setName("Ghost");
            assertThat(dao.update(po)).isZero();
        }

        @Test
        void fieldBy() {
            crud().insert(foo("Alice"), foo("Bob"));
            var changed = dao.updateFieldBy(
                    dao.table().DESCRIPTION, "partial-updated",
                    dao.table().NAME.eq("Alice"));
            assertThat(changed).isEqualTo(1);

            var alice = crud().findBy(dao.table().NAME.eq("Alice"));
            var bob = crud().findBy(dao.table().NAME.eq("Bob"));
            assertThat(alice.getDescription()).isEqualTo("partial-updated");
            assertThat(bob.getDescription()).isNotEqualTo("partial-updated");
        }

        @Test
        void fieldBy_noMatch() {
            crud().insert(foo("Alice"));
            var changed = dao.updateFieldBy(
                    dao.table().DESCRIPTION, "x",
                    dao.table().NAME.eq("Nobody"));
            assertThat(changed).isZero();
        }

        @Test
        void updateBuilder() {
            crud().insert(foo("Alice"));
            dao.update()
                    .set(dao.table().DESCRIPTION, "builder-updated")
                    .where(dao.table().NAME.eq("Alice"))
                    .execute();

            var result = crud().requireFirst();
            assertThat(result.getDescription()).isEqualTo("builder-updated");
        }
    }
}
