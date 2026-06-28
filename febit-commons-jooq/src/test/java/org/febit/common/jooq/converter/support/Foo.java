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
package org.febit.common.jooq.converter.support;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

@Data
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Builder(access = AccessLevel.PACKAGE)
public class Foo {

    public static final Foo F1 = builder()
            .id(1L)
            .title("1")
            .time(Instant.ofEpochMilli(1))
            .createdBy("user-1")
            .build();

    public static final Foo F2000 = builder()
            .id(2000L)
            .title("2000")
            .time(Instant.ofEpochMilli(2000))
            .createdBy("user-2000")
            .build();

    @Nullable
    Long id;

    @Nullable
    String title;

    @Nullable
    Instant time;

    @Nullable
    String createdBy;
}
