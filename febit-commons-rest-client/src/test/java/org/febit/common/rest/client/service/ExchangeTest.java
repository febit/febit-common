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
package org.febit.common.rest.client.service;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.febit.common.rest.client.service.apix.DemoApi;
import org.febit.common.rest.client.service.apiy.UsersApi;
import org.febit.common.rest.client.service.mvc.TestApplication;
import org.febit.common.rest.client.service.mvc.model.RequestInspectVO;
import org.febit.common.rest.client.service.mvc.model.demo.DemoForm;
import org.febit.common.rest.client.service.mvc.model.demo.DemoSearchForm;
import org.febit.common.rest.client.service.mvc.model.demo.DemoVO;
import org.febit.common.rest.client.service.mvc.model.user.UserVO;
import org.febit.lang.protocol.IResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({
        "test-exchange"
})
@SpringBootTest(classes = {
        TestApplication.class
})
class ExchangeTest {

    @Autowired
    UsersApi usersApi;

    @Autowired
    DemoApi demoApi;

    @Test
    @SuppressWarnings("DataFlowIssue")
    void demo() {
        assertThat(demoApi.ping())
                .returns(true, IResponse::isSuccess)
                .returns(null, IResponse::getData);

        assertThat(demoApi.create(new DemoForm("New Demo")))
                .returns(true, IResponse::isSuccess)
                .extracting(IResponse::getData)
                .isNotNull()
                .returns(1L, DemoVO::id)
                .returns("New Demo", DemoVO::name);

        assertThat(demoApi.requireById(123L))
                .returns(true, IResponse::isSuccess)
                .extracting(IResponse::getData)
                .isNotNull()
                .returns(123L, DemoVO::id)
                .returns("Demo 123", DemoVO::name);

        assertThat(demoApi.update(123L, new DemoForm("Updated Demo")))
                .returns(true, IResponse::isSuccess)
                .extracting(IResponse::getData)
                .isNotNull()
                .returns(123L, DemoVO::id)
                .returns("Updated Demo", DemoVO::name);

        assertThat(demoApi.search(DemoSearchForm.builder()
                .name("Demo")
                .build()))
                .returns(true, IResponse::isSuccess)
                .extracting(IResponse::getData)
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .hasSize(2);
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void user() {
        assertThat(usersApi.requireById(123L))
                .returns(true, IResponse::isSuccess)
                .extracting(IResponse::getData)
                .isNotNull()
                .returns(123L, UserVO::id)
                .returns("User 123", UserVO::name);
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void userInspect() {
        var response = usersApi.inspectQueries(
                Map.of(
                        "param1", "value1",
                        "param2", 42,
                        "param3", true,
                        "list", List.of("a", "b", "c"),
                        "nested", List.of(
                                Map.of(
                                        "n1", 1,
                                        "n2", 2
                                ),
                                "abc"
                        )
                )
        );

        assertThat(response)
                .returns(true, IResponse::isSuccess)
                .extracting(IResponse::getData)
                .isNotNull()
                .returns("GET", RequestInspectVO::method)
                .returns("/api/y/v1/users/inspect", RequestInspectVO::path)
                .satisfies(inspect -> assertThat(inspect.queries())
                        .containsEntry("param1", List.of("value1"))
                        .containsEntry("param2", List.of("42"))
                        .containsEntry("param3", List.of("true"))
                        .containsEntry("list[0]", List.of("a"))
                        .containsEntry("list[1]", List.of("b"))
                        .containsEntry("list[2]", List.of("c"))
                        .containsEntry("nested[0][n1]", List.of("1"))
                        .containsEntry("nested[0][n2]", List.of("2"))
                        .containsEntry("nested[1]", List.of("abc"))
                );
    }

}
