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
package org.febit.lang.jackson;

import lombok.experimental.UtilityClass;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.type.TypeFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;

@UtilityClass
public class JacksonTypes {

    public static final TypeFactory FACTORY = TypeFactory.createDefaultInstance();

    public static final JavaType STRING = FACTORY.constructType(String.class);
    public static final JavaType INTEGER = FACTORY.constructType(Integer.class);

    public static final JavaType MAP = FACTORY.constructMapType(
            LinkedHashMap.class, Object.class, Object.class
    );
    public static final JavaType MAP_NAMED = FACTORY.constructMapType(
            LinkedHashMap.class, String.class, Object.class
    );

    public static final JavaType LIST = FACTORY.constructCollectionLikeType(ArrayList.class, Object.class);
    public static final JavaType LIST_STRING = FACTORY.constructCollectionLikeType(ArrayList.class, String.class);

    public static final JavaType ARRAY = FACTORY.constructArrayType(Object.class);
    public static final JavaType ARRAY_STRING = FACTORY.constructArrayType(String.class);
}
