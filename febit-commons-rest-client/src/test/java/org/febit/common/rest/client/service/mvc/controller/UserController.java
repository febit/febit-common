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
package org.febit.common.rest.client.service.mvc.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.febit.common.rest.client.service.mvc.model.RequestInspectVO;
import org.febit.common.rest.client.service.mvc.model.demo.DemoVO;
import org.febit.lang.protocol.IResponse;
import org.febit.lang.util.Lists;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.TreeMap;

@RestController
@RequestMapping(
        value = "/api/y/v1/users",
        produces = "application/json"
)
public class UserController implements MockController {

    @GetMapping("/{id}")
    public IResponse<DemoVO> requireById(
            @PathVariable Long id
    ) {
        return ok(new DemoVO(id, "User " + id));
    }

    @GetMapping("/inspect")
    public IResponse<RequestInspectVO> inspect(
            HttpServletRequest request
    ) {
        var inspect = RequestInspectVO.builder();
        inspect.method(request.getMethod());
        inspect.path(request.getRequestURI());
        inspect.query(request.getQueryString());

        var queries = new TreeMap<String, List<String>>();
        request.getParameterMap().forEach((key, values) -> {
            queries.put(key, List.of(values));
        });
        inspect.queries(queries);

        var headers = new TreeMap<String, List<String>>();
        var headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            var name = headerNames.nextElement();
            var list = Lists.collect(request.getHeaders(name));
            headers.put(name.toLowerCase(), list);
        }
        inspect.headers(headers);

        return ok(inspect.build());
    }

}
