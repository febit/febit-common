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
package org.febit.common.rest.client.service.apiy;

import org.febit.common.rest.client.service.annotation.RequestParamForm;
import org.febit.common.rest.client.service.mvc.model.RequestInspectVO;
import org.febit.common.rest.client.service.mvc.model.user.UserVO;
import org.febit.lang.protocol.IResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Map;

@HttpExchange(
        value = "/api/y/v1/users",
        accept = "application/json"
)
public interface UsersApi {

    @GetExchange("/{id}")
    IResponse<UserVO> requireById(
            @PathVariable Long id
    );

    @GetExchange("/inspect")
    IResponse<RequestInspectVO> inspectQueries(
            @RequestParamForm Map<String, Object> queries
    );

}
