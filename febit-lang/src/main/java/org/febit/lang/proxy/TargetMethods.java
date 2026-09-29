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
package org.febit.lang.proxy;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;

@UtilityClass
public class TargetMethods {

    public static boolean isDefault(Method method) {
        return method.isDefault();
    }

    public static boolean isToString(Method method) {
        return "toString".equals(method.getName())
                && method.getParameterCount() == 0
                && method.getReturnType() == String.class;
    }

    public static boolean isHashCode(Method method) {
        return "hashCode".equals(method.getName())
                && method.getParameterCount() == 0
                && method.getReturnType() == int.class;
    }

    public static boolean isEquals(Method method) {
        return "equals".equals(method.getName())
                && method.getParameterCount() == 1
                && method.getReturnType() == boolean.class;
    }
}
