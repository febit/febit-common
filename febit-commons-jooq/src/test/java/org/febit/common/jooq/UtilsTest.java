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

import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UtilsTest {

    @Test
    void resolveSqlNameCamelToSnake() {
        assertThat(Utils.resolveSqlName("")).isEmpty();
        assertThat(Utils.resolveSqlName("a")).isEqualTo("a");
        assertThat(Utils.resolveSqlName("foo")).isEqualTo("foo");
        assertThat(Utils.resolveSqlName("fooBar")).isEqualTo("foo_bar");
        assertThat(Utils.resolveSqlName("FooBar")).isEqualTo("_foo_bar");
        assertThat(Utils.resolveSqlName("myFieldName")).isEqualTo("my_field_name");
        assertThat(Utils.resolveSqlName("camelCase")).isEqualTo("camel_case");
    }

    @Test
    void nameWithTableAndColumn() {
        var name = Utils.name("t_foo", "col_name");
        assertThat(name).isEqualTo(DSL.name("t_foo", "col_name"));
    }

    @Test
    void nameWithEmptyTable() {
        var name = Utils.name("", "col_name");
        assertThat(name).isEqualTo(DSL.name("col_name"));
    }

    @Test
    void fieldWithQualifiedName() {
        var field = Utils.field("t_foo.col_name", String.class);

        assertThat(field.getName()).isEqualTo("col_name");
        assertThat(field.getQualifiedName())
                .isNotNull();
    }

    @Test
    void fieldWithSimpleName() {
        var field = Utils.field("col_name", Integer.class);

        assertThat(field.getName()).isEqualTo("col_name");
    }

    @Test
    void declaredFieldsExcludesStaticFields() {
        var fields = Utils.declaredFields(WithStatic.class).toList();

        assertThat(fields)
                .extracting(java.lang.reflect.Field::getName)
                .contains("name")
                .doesNotContain("CONSTANT");
    }

    @Test
    void declaredFieldsOnEmptyClass() {
        var fields = Utils.declaredFields(EmptyClass.class).toList();
        assertThat(fields).isEmpty();
    }

    static class WithStatic {
        static final String CONSTANT = "static";
        @SuppressWarnings("unused")
        String name;
    }

    static class EmptyClass {
    }
}
