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
package org.febit.common.kafka.deser;

import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public abstract class BaseDelegatedDeserializer<T, D> implements Deserializer<T> {

    private Deserializer<D> delegated;

    protected abstract String getNameOfDelegated(boolean isKey);

    protected Deserializer<D> delegated() {
        var d = this.delegated;
        if (d != null) {
            return d;
        }
        throw new IllegalStateException("No delegated deser available, should call configure before use it.");
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        this.delegated = resolveDelegated(configs, isKey);
    }

    protected Deserializer<D> resolveDelegated(Map<String, ?> configs, boolean isKey) {
        var key = getNameOfDelegated(isKey);
        var cls = configs.get(key);
        if (cls == null) {
            throw new IllegalArgumentException("Delegated deser is required, please given a deser class name by: " + key);
        }
        return DeserializerUtils.create(cls.toString(), configs, isKey);
    }

    @Override
    public void close() {
        delegated().close();
    }
}
