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
package org.febit.common.jooq.converter;

import jakarta.annotation.Nullable;
import org.febit.lang.Valued;
import org.febit.lang.util.TypeParameters;
import org.jooq.impl.AbstractConverter;

import java.io.Serializable;
import java.util.Map;

@SuppressWarnings({"unused"})
public class ValuedEnumConverter<DO extends Serializable, V extends Valued<DO> & Serializable>
        extends AbstractConverter<DO, V> {

    @SuppressWarnings({
            "squid:S1948" // MISDIAGNOSE: Fields in a "Serializable" class should either be transient or serializable
    })
    private final Map<DO, V> mapping;

    public ValuedEnumConverter(Class<DO> dbType, Class<V> enumType) {
        super(dbType, enumType);
        this.mapping = Valued.mapping(enumType.getEnumConstants());
    }

    public static <DO extends Serializable, E extends Valued<DO> & Serializable>
    ValuedEnumConverter<DO, E> forEnum(Class<E> enumType) {
        Class<DO> keyClass = TypeParameters.resolve(enumType, Valued.class, 0);
        if (keyClass == null) {
            throw new IllegalArgumentException("Only Valued<?> Enums are accepted,"
                    + " or cannot resolve first generic type: " + enumType);
        }
        return new ValuedEnumConverter<>(keyClass, enumType);
    }

    @Nullable
    @Override
    public V from(@Nullable DO dbObj) {
        return dbObj == null ? null
                : this.mapping.get(dbObj);
    }

    @Nullable
    @Override
    public DO to(@Nullable V customObj) {
        return customObj == null ? null
                : customObj.getValue();
    }
}
