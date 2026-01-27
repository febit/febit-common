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
package org.febit.common.rest.client.service.mvc;

import org.febit.common.rest.client.RestClientStandardBuilder;
import org.febit.common.rest.client.service.ApiGroups;
import org.febit.common.rest.client.service.ExchangeIgnoredArgumentResolver;
import org.febit.common.rest.client.service.RequestParamFormArgumentResolver;
import org.febit.common.rest.client.service.apix.DemoApi;
import org.febit.common.rest.client.service.apiy.UsersApi;
import org.febit.common.rest.client.service.mvc.controller.MockController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.client.MockMvcClientHttpRequestFactory;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.HttpServiceGroup;
import org.springframework.web.service.registry.ImportHttpServices;
import tools.jackson.databind.json.JsonMapper;

@ImportHttpServices(
        group = ApiGroups.X,
        clientType = HttpServiceGroup.ClientType.REST_CLIENT,
        basePackageClasses = {
                DemoApi.class
        }
)
@ImportHttpServices(
        group = ApiGroups.Y,
        clientType = HttpServiceGroup.ClientType.REST_CLIENT,
        basePackageClasses = {
                UsersApi.class
        }
)
@Configuration
public class HttpServiceConfig {

    @Bean
    RestClientHttpServiceGroupConfigurer myHttpServiceGroupConfigurer(
            ObjectProvider<MockController> controllers
    ) {
        var jsonMapper = JsonMapper.builder().build();
        var mockMvc = MockMvcBuilders.standaloneSetup(
                        controllers.stream().toArray()
                )
                .build();
        return all -> {
            all.forEachProxyFactory((group, builder) -> {
                builder.customArgumentResolver(RequestParamFormArgumentResolver.create(jsonMapper));
                builder.customArgumentResolver(new ExchangeIgnoredArgumentResolver());
            });

            var x = all.filterByName(ApiGroups.X);
            x.forEachClient(group -> RestClientStandardBuilder.create()
                    .requestFactory(new MockMvcClientHttpRequestFactory(mockMvc))
                    .baseUrl("http://localhost:8080")
            );

            var y = all.filterByName(ApiGroups.Y);
            y.forEachClient(group -> RestClientStandardBuilder.create()
                    .requestFactory(new MockMvcClientHttpRequestFactory(mockMvc))
                    .baseUrl("http://localhost:8181")
            );
        };
    }
}
