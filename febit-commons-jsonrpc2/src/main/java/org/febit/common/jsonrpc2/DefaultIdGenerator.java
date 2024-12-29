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
package org.febit.common.jsonrpc2;

import lombok.RequiredArgsConstructor;
import org.febit.common.jsonrpc2.protocol.Id;

import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor(staticName = "create")
public class DefaultIdGenerator implements IdGenerator {

    private final AtomicLong next;

    @Override
    public Id next() {
        return Id.of(next.getAndIncrement());
    }

    public static DefaultIdGenerator create() {
        return startFrom(1L);
    }

    public static DefaultIdGenerator startFrom(long initial) {
        return create(new AtomicLong(initial));
    }
}
