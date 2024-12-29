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
package org.febit.lang.util.proxy;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;
import java.util.Optional;

@UtilityClass
public class Invokers {

    public static <T> Optional<Invoker<T>> defaultForInterface(Method method) {
        if (TargetMethods.isDefault(method)) {
            return Optional.of(Invokers.passthrough(method));
        }
        return switch (method.getName()) {
            case "toString" -> TargetMethods.isToString(method)
                    ? Optional.of(toIdentityString())
                    : Optional.empty();
            case "hashCode" -> TargetMethods.isHashCode(method)
                    ? Optional.of(identityHashCode())
                    : Optional.empty();
            case "equals" -> TargetMethods.isEquals(method)
                    ? Optional.of(isSameObject())
                    : Optional.empty();
            default -> Optional.empty();
        };
    }

    public static <T> Invoker<T> passthrough(Method method) {
        return method::invoke;
    }

    public static <T> Invoker<T> isSameObject() {
        return (self, args) -> {
            if (args.length != 1) {
                return false;
            }
            return self == args[0];
        };
    }

    public static <T> Invoker<T> identityHashCode() {
        return (self, args) -> System.identityHashCode(self);
    }

    public static <T> Invoker<T> toIdentityString() {
        return (self, args) -> self.getClass().getName()
                + "@"
                + Integer.toHexString(System.identityHashCode(self));
    }

    public static <T> Invoker<T> toStaticString(String value) {
        return (self, args) -> value;
    }
}
