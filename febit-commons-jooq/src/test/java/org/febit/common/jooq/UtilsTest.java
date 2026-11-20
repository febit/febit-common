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
import org.tabletest.junit.TableTest;

import org.febit.common.jooq.annotation.CriterionMapping;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @TableTest("""
            input       | expected
            ""          | ""
            a           | a
            foo         | foo
            fooBar      | foo_bar
            FooBar      | _foo_bar
            myFieldName | my_field_name
            camelCase   | camel_case
            """)
    void resolveSqlName(String input, String expected) {
        assertEquals(expected, Utils.resolveSqlName(input));
    }

    @Test
    void nameWithTableAndColumn() {
        var name = Utils.nameOf("t_foo", "col_name");
        assertThat(name).isEqualTo(DSL.name("t_foo", "col_name"));
    }

    @Test
    void nameWithEmptyTable() {
        var name = Utils.nameOf("", "col_name");
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
    void nameOfMappingUsesExplicitValue() throws Exception {
        var anno = Annotated.class.getDeclaredField("explicit").getAnnotation(CriterionMapping.class);

        assertThat(Utils.nameOf(anno, "explicit")).isEqualTo(DSL.name("t_foo", "col_name"));
    }

    @Test
    void nameOfMappingFallsBackToFieldName() throws Exception {
        var anno = Annotated.class.getDeclaredField("noValue").getAnnotation(CriterionMapping.class);

        assertThat(Utils.nameOf(anno, "noValue")).isEqualTo(DSL.name("t_foo", "no_value"));
    }

    @Test
    void nameOfNullMappingFallsBackToSnakeCase() {
        assertThat(Utils.nameOf((CriterionMapping) null, "fooBar"))
                .isEqualTo(DSL.name("foo_bar"));
    }

    @Test
    void nameOfNestedNameAnnotation() throws Exception {
        var anno = Annotated.class.getDeclaredField("withName").getAnnotation(CriterionMapping.class);

        assertThat(Utils.nameOf(anno.names()[0])).isEqualTo(DSL.name("t_bar", "name_col"));
    }

    @Test
    void declaredFieldsExcludesStaticFields() {
        var fields = Utils.declaredFields(WithStatic.class).toList();

        assertThat(fields)
                .extracting(Field::getName)
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

    static class Annotated {
        @CriterionMapping(table = "t_foo", value = "col_name")
        String explicit;

        @CriterionMapping(table = "t_foo")
        String noValue;

        @CriterionMapping(names = @CriterionMapping.Name(table = "t_bar", value = "name_col"))
        String withName;
    }
}
