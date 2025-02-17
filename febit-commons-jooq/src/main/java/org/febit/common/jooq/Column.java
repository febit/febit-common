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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    String table() default "";

    String value() default "";

    String[] values() default {};

    Name[] names() default {};

    boolean ignoreEmpty() default true;

    boolean ignoreCase() default false;

    Operator operator() default Operator.NONE;

    @Getter
    @RequiredArgsConstructor
    enum Operator {
        NONE("none"),

        KEYWORD("Keyword"),

        STARTS_WITH("Starts With"),
        ENDS_WITH("Ends With"),
        CONTAINS("Contains"),
        NOT_CONTAINS("Not Contains"),

        IN("In"),
        NOT_IN("Not In"),

        IS_NULL("Is Null"),
        IS_NOT_NULL("Is Not Null"),

        EQ("=="),
        GT(">"),
        GE(">="),
        LT("<"),
        LE("<="),
        ;

        private final String title;

        public boolean multiValues() {
            return this == IN || this == NOT_IN;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Name {
        String table() default "";

        String value();
    }
}
