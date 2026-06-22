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
import org.febit.common.jooq.foo.FooStatus;
import org.febit.common.jooq.foo.FooTestSupport;
import org.febit.common.jooq.foo.TFoo;
import org.febit.lang.protocol.Pagination;
import org.febit.lang.protocol.Sort;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IBasicQueryDaoTest {

    static class FooBasicQueryDao extends BaseDao<TFoo, FooPO, FooRecord>
            implements IBasicQueryDao<TFoo, FooPO, FooRecord> {

        public FooBasicQueryDao(Configuration conf) {
            super(conf);
        }
    }

    @Nested
    class FooFindBy extends FooTestSupport {
        private final FooBasicQueryDao dao = new FooBasicQueryDao(conf());

        @Test
        void bySingleCondition() {
            crud().insert(foo("Alice"));
            var found = dao.findBy(dao.table().NAME.eq("Alice"));
            assertThat(found).isNotNull()
                    .extracting(FooPO::getName)
                    .isEqualTo("Alice");
        }

        @Test
        void byMultipleConditions() {
            crud().insert(foo("Alice", FooStatus.CREATED));
            var found = dao.findBy(
                    dao.table().NAME.eq("Alice"),
                    dao.table().STATUS.eq(FooStatus.CREATED)
            );
            assertThat(found).isNotNull();
        }

        @Test
        void noMatch() {
            assertThat(dao.findBy(dao.table().NAME.eq("Nobody"))).isNull();
        }

        @Test
        void byEnumStatus() {
            crud().insert(foo("Alice", FooStatus.FAILED));
            var found = dao.findBy(dao.table().STATUS.eq(FooStatus.FAILED));
            assertThat(found).isNotNull()
                    .extracting(FooPO::getStatus)
                    .isEqualTo(FooStatus.FAILED);
        }

        @Test
        void byNullFieldValue() {
            crud().insert(foo("Alice"));
            assertThat(dao.findBy(dao.table().NAME, (String) null)).isNull();
        }

        @Test
        void byFieldValue() {
            crud().insert(foo("Alice"));
            var found = dao.findBy(dao.table().NAME, "Alice");
            assertThat(found).isNotNull()
                    .extracting(FooPO::getName)
                    .isEqualTo("Alice");
        }
    }

    @Nested
    class FooFindByMapperFirst extends FooTestSupport {
        private final FooBasicQueryDao dao = new FooBasicQueryDao(conf());

        @Test
        void found() {
            crud().insert(foo("Alice"));
            var name = dao.findBy(
                    r -> r.get(TFoo.FOO.NAME),
                    dao.table().NAME.eq("Alice"));
            assertThat(name).isEqualTo("Alice");
        }

        @Test
        void noMatch() {
            var result = dao.findBy(
                    r -> r.get(TFoo.FOO.NAME),
                    dao.table().NAME.eq("Nobody"));
            assertThat(result).isNull();
        }
    }

    @Nested
    class FooFindByListCondition extends FooTestSupport {
        private final FooBasicQueryDao dao = new FooBasicQueryDao(conf());

        @Test
        void found() {
            crud().insert(foo("Alice"));
            List<Condition> conditions = List.of(dao.table().NAME.eq("Alice"));
            var found = dao.findBy(conditions);
            assertThat(found).isNotNull()
                    .extracting(FooPO::getName)
                    .isEqualTo("Alice");
        }

        @Test
        void noMatch() {
            List<Condition> conditions = List.of(dao.table().NAME.eq("Nobody"));
            assertThat(dao.findBy(conditions)).isNull();
        }
    }

    @Nested
    class FooListByMapperFirst extends FooTestSupport {
        private final FooBasicQueryDao dao = new FooBasicQueryDao(conf());

        @Test
        void extractNames() {
            crud().insert(foo("Alice"), foo("Bob"));
            var names = dao.listBy(
                    r -> r.get(TFoo.FOO.NAME),
                    dao.table().NAME.eq("Alice"));
            assertThat(names).containsExactly("Alice");
        }
    }

    @Nested
    class FooListByListCondition extends FooTestSupport {
        private final FooBasicQueryDao dao = new FooBasicQueryDao(conf());

        @Test
        void filtered() {
            crud().insert(foo("Alice"), foo("Bob"));
            List<Condition> conditions = List.of(dao.table().NAME.eq("Alice"));
            var result = dao.listBy(conditions);
            assertThat(result).hasSize(1)
                    .first().extracting(FooPO::getName).isEqualTo("Alice");
        }

        @Test
        void noMatch() {
            List<Condition> conditions = List.of(dao.table().NAME.eq("Nobody"));
            assertThat(dao.listBy(conditions)).isEmpty();
        }
    }

    @Nested
    class FooListFieldByListCondition extends FooTestSupport {
        private final FooBasicQueryDao dao = new FooBasicQueryDao(conf());

        @Test
        void filtered() {
            crud().insert(foo("Alice"), foo("Bob"));
            List<Condition> conditions = List.of(dao.table().NAME.eq("Alice"));
            var names = dao.listFieldBy(dao.table().NAME, conditions);
            assertThat(names).containsExactly("Alice");
        }
    }

    @Nested
    class FooSearchForm extends FooTestSupport {
        private final FooBasicQueryDao dao = new FooBasicQueryDao(conf());

        record NameSearchForm(
                @Equals(TFoo.Columns.NAME) String name
        ) implements SearchForm {
        }

        record NameContainsForm(
                @Contains(TFoo.Columns.NAME) String name
        ) implements SearchForm {
        }

        record NameNotContainsForm(
                @NotContains(TFoo.Columns.NAME) String name
        ) implements SearchForm {
        }

        record NameStartsWithForm(
                @StartsWith(TFoo.Columns.NAME) String name
        ) implements SearchForm {
        }

        record NameEndsWithForm(
                @EndsWith(TFoo.Columns.NAME) String name
        ) implements SearchForm {
        }

        record NameIsNullForm(
                @IsNull(TFoo.Columns.NAME) Boolean noName
        ) implements SearchForm {
        }

        record NameIsNotNullForm(
                @IsNotNull(TFoo.Columns.NAME) Boolean hasName
        ) implements SearchForm {
        }

        record NameInForm(
                @In(TFoo.Columns.NAME)
                List<String> names
        ) implements SearchForm {
        }

        record NameNotInForm(
                @NotIn(TFoo.Columns.NAME) List<String> names
        ) implements SearchForm {
        }

        record NameGtForm(
                @GreaterThan(TFoo.Columns.NAME) String name
        ) implements SearchForm {
        }

        record NameGeForm(
                @GreaterEquals(TFoo.Columns.NAME) String name
        ) implements SearchForm {
        }

        record NameLtForm(
                @LessThan(TFoo.Columns.NAME) String name
        ) implements SearchForm {
        }

        record NameLeForm(
                @LessEquals(TFoo.Columns.NAME) String name
        ) implements SearchForm {
        }

        record NameKeywordForm(
                @Keyword({
                        TFoo.Columns.NAME
                })
                String keyword
        ) implements SearchForm {
        }

        record DescriptionNullForm(
                @IsNull(TFoo.Columns.DESCRIPTION) Boolean noDescription
        ) implements SearchForm {
        }

        record CreatedByNullForm(
                @IsNull(
                        TFoo.Columns.CREATED_BY
                )
                Boolean noCreatedBy
        ) implements SearchForm {
        }

        @Test
        void findByEq() {
            crud().insert(foo("Alice"), foo("Bob"));
            var found = dao.findBy(new NameSearchForm("Alice"));
            assertThat(found).isNotNull()
                    .extracting(FooPO::getName)
                    .isEqualTo("Alice");
        }

        @Test
        void findByEqNoMatch() {
            crud().insert(foo("Alice"));
            assertThat(dao.findBy(new NameSearchForm("Nobody"))).isNull();
        }

        @Test
        void findByContains() {
            crud().insert(foo("Alice"), foo("Bob"));
            var found = dao.findBy(new NameContainsForm("lic"));
            assertThat(found).isNotNull()
                    .extracting(FooPO::getName)
                    .isEqualTo("Alice");
        }

        @Test
        void findByNotContains() {
            crud().insert(foo("Alice"), foo("Bob"));
            var found = dao.findBy(new NameNotContainsForm("Ali"));
            assertThat(found).isNotNull()
                    .extracting(FooPO::getName)
                    .isEqualTo("Bob");
        }

        @Test
        void findByStartsWith() {
            crud().insert(foo("Alice"), foo("Bob"));
            var found = dao.findBy(new NameStartsWithForm("Ali"));
            assertThat(found).isNotNull()
                    .extracting(FooPO::getName)
                    .isEqualTo("Alice");
        }

        @Test
        void findByEndsWith() {
            crud().insert(foo("Alice"), foo("Bob"));
            var found = dao.findBy(new NameEndsWithForm("ice"));
            assertThat(found).isNotNull()
                    .extracting(FooPO::getName)
                    .isEqualTo("Alice");
        }

        @Test
        void findByIsNull() {
            crud().insert(foo("Alice"));
            assertThat(dao.findBy(new CreatedByNullForm(true)))
                    .isNotNull()
                    .extracting(FooPO::getName)
                    .isEqualTo("Alice");
            assertThat(dao.findBy(new CreatedByNullForm(false)))
                    .isNull();
        }

        @Test
        void findByIsNotNull() {
            crud().insert(foo("Alice"));
            var found = dao.findBy(new NameIsNotNullForm(true));
            assertThat(found).isNotNull();
        }

        @Test
        void in() {
            crud().insert(foo("Alice"), foo("Bob"), foo("Charlie"));
            var found = dao.listBy(new NameInForm(List.of("Alice", "Bob")));
            assertThat(found).isNotNull();
        }

        @Test
        void findByNotIn() {
            crud().insert(foo("Alice"), foo("Bob"));
            var found = dao.findBy(new NameNotInForm(List.of("Alice")));
            assertThat(found)
                    .isNotNull()
                    .extracting(FooPO::getName)
                    .isEqualTo("Bob");
        }

        @Test
        void gt() {
            crud().insert(foo("Alice"), foo("Bob"), foo("Charlie"));
            var found = dao.listBy(new NameGtForm("Alice"));
            assertThat(found)
                    .isNotNull()
                    .extracting(FooPO::getName)
                    .contains("Bob");
        }

        @Test
        void ge() {
            crud().insert(foo("Alice"), foo("Bob"));
            var found = dao.listBy(new NameGeForm("Alice"));
            assertThat(found)
                    .isNotNull()
                    .extracting(FooPO::getName)
                    .contains("Alice");
        }

        @Test
        void findByLt() {
            crud().insert(foo("Alice"), foo("Bob"));
            var found = dao.findBy(new NameLtForm("Bob"));
            assertThat(found).isNotNull()
                    .extracting(FooPO::getName)
                    .isEqualTo("Alice");
        }

        @Test
        void findByLe() {
            crud().insert(foo("Alice"), foo("Bob"));
            var found = dao.findBy(new NameLeForm("Alice"));
            assertThat(found).isNotNull()
                    .extracting(FooPO::getName)
                    .isEqualTo("Alice");
        }

        @Test
        void findByKeyword() {
            crud().insert(foo("Alice"), foo("Bob"));
            var found = dao.findBy(new NameKeywordForm("Ali"));
            assertThat(found).isNotNull()
                    .extracting(FooPO::getName)
                    .isEqualTo("Alice");
        }

        @Test
        void findByKeywordNoMatch() {
            crud().insert(foo("Alice"));
            assertThat(dao.findBy(new NameKeywordForm("Bob"))).isNull();
        }

        @Test
        void findByWithMapper() {
            crud().insert(foo("Alice"));
            var name = dao.findBy(new NameSearchForm("Alice"),
                    r -> r.get(TFoo.FOO.NAME));
            assertThat(name).isEqualTo("Alice");
        }

        @Test
        void listByEq() {
            crud().insert(foo("Alice"), foo("Bob"));
            var result = dao.listBy(new NameSearchForm("Alice"));
            assertThat(result).hasSize(1);
        }

        @Test
        void listByContains() {
            crud().insert(foo("Alice"), foo("Bob"));
            var result = dao.listBy(new NameContainsForm("Bob"));
            assertThat(result).hasSize(1)
                    .first().extracting(FooPO::getName).isEqualTo("Bob");
        }

        @Test
        void listByWithMapper() {
            crud().insert(foo("Alice"), foo("Bob"));
            var names = dao.listBy(new EmptySearchForm(),
                    r -> r.get(TFoo.FOO.NAME));
            assertThat(names)
                    .containsExactlyInAnyOrder("Alice", "Bob");
        }

        @Test
        void listFieldByEq() {
            crud().insert(foo("Alice"), foo("Bob"));
            var names = dao.listFieldBy(dao.table().NAME,
                    new NameSearchForm("Alice"));
            assertThat(names).containsExactly("Alice");
        }

        @Test
        void countByEq() {
            crud().insert(foo("Alice"), foo("Bob"));
            assertThat(dao.countBy(new NameSearchForm("Alice"))).isEqualTo(1L);
        }

        @Test
        void countByContains() {
            crud().insert(foo("Alice"), foo("Bob"));
            assertThat(dao.countBy(new NameContainsForm("lic"))).isEqualTo(1L);
        }

        @Test
        void existsByEq() {
            crud().insert(foo("Alice"));
            assertThat(dao.existsBy(new NameSearchForm("Alice"))).isTrue();
            assertThat(dao.existsBy(new NameSearchForm("Nobody"))).isFalse();
        }

        @Test
        void existsByContains() {
            crud().insert(foo("Alice"));
            assertThat(dao.existsBy(new NameContainsForm("lic"))).isTrue();
            assertThat(dao.existsBy(new NameContainsForm("xyz"))).isFalse();
        }

        @Test
        void pageFilter() {
            crud().insert(foo("Alice"), foo("Bob"), foo("Charlie"));
            var result = dao.page(Pagination.of(1, 10), new NameSearchForm("Alice"));
            assertThat(result.getMeta().getTotal()).isEqualTo(1L);
            assertThat(result.getRows()).hasSize(1)
                    .first().extracting(FooPO::getName).isEqualTo("Alice");
        }

        @Test
        void pageWithMapper() {
            crud().insert(foo("Alice"), foo("Bob"));
            var result = dao.page(
                    Pagination.of(1, 10),
                    new EmptySearchForm(),
                    r -> r.get(TFoo.FOO.NAME)
            );
            assertThat(result.getMeta().getTotal()).isEqualTo(2L);
            assertThat(result.getRows())
                    .containsExactlyInAnyOrder("Alice", "Bob");
        }
    }

    @Nested
    class FooListAll extends FooTestSupport {
        private final FooBasicQueryDao dao = new FooBasicQueryDao(conf());

        @Test
        void empty() {
            assertThat(dao.listAll()).isEmpty();
        }

        @Test
        void withData() {
            crud().insert(foo("Alice"), foo("Bob"), foo("Charlie"));
            assertThat(dao.listAll()).hasSize(3);
        }
    }

    @Nested
    class FooListBy extends FooTestSupport {
        private final FooBasicQueryDao dao = new FooBasicQueryDao(conf());

        @Test
        void byName() {
            crud().insert(foo("Alice"), foo("Bob"));
            var result = dao.listBy(dao.table().NAME.eq("Alice"));
            assertThat(result).hasSize(1)
                    .first().extracting(FooPO::getName).isEqualTo("Alice");
        }

        @Test
        void byStatus() {
            crud().insert(foo("A", FooStatus.CREATED), foo("B", FooStatus.RUNNING));
            var result = dao.listBy(dao.table().STATUS.eq(FooStatus.RUNNING));
            assertThat(result).hasSize(1)
                    .first().extracting(FooPO::getStatus).isEqualTo(FooStatus.RUNNING);
        }

        @Test
        void noMatch() {
            assertThat(dao.listBy(dao.table().NAME.eq("Nobody"))).isEmpty();
        }
    }

    @Nested
    class FooListFieldBy extends FooTestSupport {
        private final FooBasicQueryDao dao = new FooBasicQueryDao(conf());

        @Test
        void names() {
            crud().insert(foo("Alice"), foo("Bob"));
            var names = dao.listFieldBy(dao.table().NAME);
            assertThat(names).containsExactlyInAnyOrder("Alice", "Bob");
        }

        @Test
        void filtered() {
            crud().insert(foo("Alice"), foo("Bob"));
            var names = dao.listFieldBy(dao.table().NAME,
                    dao.table().NAME.eq("Bob"));
            assertThat(names).containsExactly("Bob");
        }
    }

    @Nested
    class FooFindFieldBy extends FooTestSupport {
        private final FooBasicQueryDao dao = new FooBasicQueryDao(conf());

        @Test
        void found() {
            crud().insert(foo("Alice"));
            var id = crud().requireFirst().getId();
            var name = dao.findFieldBy(dao.table().NAME,
                    dao.table().ID.eq(id));
            assertThat(name).isEqualTo("Alice");
        }

        @Test
        void noMatch() {
            crud().insert(foo("Alice"));
            var name = dao.findFieldBy(dao.table().NAME,
                    dao.table().ID.eq(-1L));
            assertThat(name).isNull();
        }
    }

    @Nested
    class FooCountBy extends FooTestSupport {
        private final FooBasicQueryDao dao = new FooBasicQueryDao(conf());

        @Test
        void all() {
            crud().insert(foo("A"), foo("B"));
            assertThat(dao.countBy()).isEqualTo(2L);
        }

        @Test
        void byCondition() {
            crud().insert(foo("Alice"), foo("Bob"));
            assertThat(dao.countBy(dao.table().NAME.eq("Alice"))).isEqualTo(1L);
        }

        @Test
        void empty() {
            assertThat(dao.countBy()).isZero();
        }
    }

    @Nested
    class FooExistsBy extends FooTestSupport {
        private final FooBasicQueryDao dao = new FooBasicQueryDao(conf());

        @Test
        void true_() {
            crud().insert(foo("Alice"));
            assertThat(dao.existsBy(dao.table().NAME.eq("Alice"))).isTrue();
        }

        @Test
        void false_() {
            assertThat(dao.existsBy(dao.table().NAME.eq("Nobody"))).isFalse();
        }
    }

    @Nested
    class FooPage extends FooTestSupport {
        private final FooBasicQueryDao dao = new FooBasicQueryDao(conf());

        @OrderMappingBy(PageSortMapping.class)
        record PageSortableForm(
                @Equals String name
        ) implements SearchForm {
        }

        static class PageSortMapping {
            String id;
            @Column("created_at")
            String createdAt;
            @Column("updated_at")
            String updatedAt;
        }

        @Test
        void firstPage() {
            crud().insert(foo("A"), foo("B"), foo("C"), foo("D"), foo("E"));
            var result = dao.page(Pagination.of(1, 2), new EmptySearchForm());
            assertThat(result.getMeta().getTotal()).isEqualTo(5L);
            assertThat(result.getRows()).hasSize(2);
        }

        @Test
        void secondPage() {
            crud().insert(foo("A"), foo("B"), foo("C"), foo("D"), foo("E"));
            var result = dao.page(Pagination.of(2, 2), new EmptySearchForm());
            assertThat(result.getMeta().getTotal()).isEqualTo(5L);
            assertThat(result.getRows()).hasSize(2);
        }

        @Test
        void lastPage() {
            crud().insert(foo("A"), foo("B"), foo("C"));
            var result = dao.page(Pagination.of(2, 2), new EmptySearchForm());
            assertThat(result.getMeta().getTotal()).isEqualTo(3L);
            assertThat(result.getRows()).hasSize(1);
        }

        @Test
        void empty() {
            var result = dao.page(Pagination.of(1, 10), new EmptySearchForm());
            assertThat(result.getMeta().getTotal()).isZero();
            assertThat(result.getRows()).isEmpty();
        }

        @Test
        void beyondTotalOffset() {
            crud().insert(foo("A"), foo("B"));
            var result = dao.page(Pagination.of(3, 2), new EmptySearchForm());
            assertThat(result.getMeta().getTotal()).isEqualTo(2L);
            assertThat(result.getRows()).isEmpty();
        }

        @Test
        void withSortOrders() {
            crud().insert(foo("B"), foo("A"), foo("C"));
            var pagination = new Pagination(1, 10, List.of(Sort.asc("id")));
            var result = dao.page(pagination, new PageSortableForm(null));

            assertThat(result.getMeta().getTotal()).isEqualTo(3L);
            assertThat(result.getRows()).hasSize(3);
        }
    }

    record EmptySearchForm() implements SearchForm {
    }
}
