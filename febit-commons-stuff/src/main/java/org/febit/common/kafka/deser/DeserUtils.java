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

import com.fasterxml.jackson.databind.JavaType;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.utils.Utils;
import org.febit.lang.UncheckedException;

import javax.annotation.Nullable;
import java.util.Map;

import static org.febit.lang.util.JacksonUtils.TYPE_FACTORY;

@UtilityClass
public class DeserUtils {

    private static final Map<String, String> BUILD_IN_DESER;

    static {
        BUILD_IN_DESER = Map.of(
                "discard", DiscardDeser.class.getName(),
                "failsafe", FailsafeDeser.class.getName(),
                "access-log", AccessLogDeser.class.getName(),
                "string", StringDeser.class.getName(),
                "json", JsonDeser.class.getName()
        );
    }

    @Nullable
    public static JavaType resolveJavaType(Map<String, ?> configs, String key) {
        var type = configs.get(key);
        if (type == null) {
            return null;
        }

        Class<?> cls;
        try {
            cls = Utils.loadClass(type.toString(), Object.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to load type: " + type);
        }
        return TYPE_FACTORY.constructType(cls);
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Deserializer<?>> resolveDeserClass(@Nullable String deser)
            throws ClassNotFoundException {
        if (StringUtils.isEmpty(deser)) {
            return StringDeser.class;
        }
        var name = BUILD_IN_DESER.getOrDefault(deser, deser);
        return (Class<? extends Deserializer<?>>) Utils.loadClass(name, Deserializer.class);
    }

    public static <T> Deserializer<T> create(String cls, Map<String, ?> configs, boolean isKey) {
        try {
            var deserClass = resolveDeserClass(cls);
            @SuppressWarnings("unchecked")
            Deserializer<T> deser = (Deserializer<T>) Utils.newInstance(deserClass, Deserializer.class);
            deser.configure(configs, isKey);
            return deser;
        } catch (ClassNotFoundException e) {
            throw new UncheckedException("Unable to create deser: " + cls
                    + ", class not found: " + e.getMessage()
            );
        }
    }

}
