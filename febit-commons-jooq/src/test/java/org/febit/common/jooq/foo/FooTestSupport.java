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

import lombok.Getter;
import lombok.experimental.Accessors;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.jpa.extensions.DefaultAnnotatedPojoMemberProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Accessors(fluent = true)
public abstract class FooTestSupport {

    private static final AtomicInteger DB_SEQ = new AtomicInteger(1);

    @Getter
    private final Configuration conf;
    @Getter
    private final FooDao crud;

    {
        var ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test_foo_" + DB_SEQ.incrementAndGet() + ";DB_CLOSE_DELAY=-1");
        ds.setUser("test");
        ds.setPassword("foo");
        conf = new DefaultConfiguration()
                .set(ds)
                .set(SQLDialect.H2)
                .set(new DefaultAnnotatedPojoMemberProvider());
        crud = new FooDao(conf);
    }

    @BeforeEach
    void baseSetUp() {
        var dsl = conf().dsl();
        dsl.execute(DDL.CREATE_TABLE);
    }

    @AfterEach
    void baseTearDown() {
        conf().dsl().execute(DDL.DROP_TABLE);
    }

    protected FooPO foo(String name) {
        return foo(name, FooStatus.CREATED);
    }

    protected FooPO foo(String name, FooStatus status) {
        var bean = new JsonBean();
        bean.setName("j-" + name);
        bean.setTitle("t-" + name);
        bean.setEnabled(true);

        return FooPO.builder()
                .enabled(true)
                .name(name)
                .status(status)
                .description("desc-" + name)
                .date(LocalDate.now())
                .time(LocalTime.now())
                .timestamp(Instant.now())
                .jsonVarchar(bean)
                .jsonText(bean)
                .stringJsonBean(bean)
                .stringJsonBeanArray(new JsonBean[]{bean})
                .stringJsonBeanList(List.of(bean))
                .stringJsonBeanMap(Map.of(1, bean))
                .stringJsonMap(Map.of(1, true))
                .build();
    }
}
