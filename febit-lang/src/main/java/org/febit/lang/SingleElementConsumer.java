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
package org.febit.lang;

import lombok.Getter;

import java.util.function.Consumer;

/**
 * Consumer that only accept single element.
 *
 * @param <T> the type of the input to the operation
 */
public class SingleElementConsumer<T> implements Consumer<T> {

    @Getter
    private T value;

    @Override
    public void accept(T next) {
        if (value != null) {
            throw new IllegalArgumentException("Except only one element, but got one more.");
        }
        this.value = next;
    }
}
