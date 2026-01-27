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
package org.febit.common.rest.client.service.apix;

import org.febit.common.rest.client.service.annotation.RequestParamForm;
import org.febit.common.rest.client.service.mvc.model.demo.DemoForm;
import org.febit.common.rest.client.service.mvc.model.demo.DemoSearchForm;
import org.febit.common.rest.client.service.mvc.model.demo.DemoVO;
import org.febit.lang.protocol.IResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange(
        value = "/api/x/v1/demo",
        accept = "application/json"
)
public interface DemoApi {

    @GetExchange("/ping")
    IResponse<Void> ping();

    @GetExchange("/{id}")
    IResponse<DemoVO> requireById(
            @PathVariable Long id
    );

    @GetExchange
    IResponse<List<DemoVO>> search(
            @RequestParamForm DemoSearchForm form
    );

    @PostExchange
    IResponse<DemoVO> create(
            @RequestBody DemoForm form
    );

    @PatchExchange("/{id}")
    IResponse<DemoVO> update(
            @PathVariable Long id,
            @RequestBody DemoForm form
    );

}
