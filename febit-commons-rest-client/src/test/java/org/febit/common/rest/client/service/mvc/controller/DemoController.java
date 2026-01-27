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

import org.febit.common.rest.client.service.annotation.RequestParamForm;
import org.febit.common.rest.client.service.mvc.model.demo.DemoForm;
import org.febit.common.rest.client.service.mvc.model.demo.DemoSearchForm;
import org.febit.common.rest.client.service.mvc.model.demo.DemoVO;
import org.febit.lang.protocol.IResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(
        value = "/api/x/v1/demo",
        produces = "application/json"
)
public class DemoController implements MockController {

    @GetMapping("ping")
    public IResponse<Void> ping() {
        return ok(null);
    }

    @GetMapping("/{id}")
    public IResponse<DemoVO> requireById(
            @PathVariable Long id
    ) {
        return ok(new DemoVO(id, "Demo " + id));
    }

    @GetMapping
    public IResponse<List<DemoVO>> search(
            @RequestParamForm DemoSearchForm form
    ) {
        List<DemoVO> results = List.of(
                new DemoVO(1L, "Demo 1"),
                new DemoVO(2L, "Demo 2")
        );
        return ok(results);
    }

    @PostMapping
    public IResponse<DemoVO> create(
            @RequestBody DemoForm form
    ) {
        DemoVO created = new DemoVO(1L, form.name());
        return ok(created);
    }

    @PatchMapping("/{id}")
    public IResponse<DemoVO> update(
            @PathVariable Long id,
            @RequestBody DemoForm form
    ) {
        DemoVO updated = new DemoVO(id, form.name());
        return ok(updated);
    }

}
