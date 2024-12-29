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
package org.febit.lang.util;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

@UtilityClass
public class ReflectUtils {

    public static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    public static boolean isNotStatic(Member member) {
        return !isStatic(member);
    }

    public static boolean isPublic(Member member) {
        return Modifier.isPublic(member.getModifiers());
    }

    public static boolean isNotPublic(Member member) {
        return !isPublic(member);
    }

    public static boolean isProtected(Member member) {
        return Modifier.isProtected(member.getModifiers());
    }

    public static boolean isNotProtected(Member member) {
        return !isProtected(member);
    }

    public static boolean isPrivate(Member member) {
        return Modifier.isPrivate(member.getModifiers());
    }

    public static boolean isNotPrivate(Member member) {
        return !isPrivate(member);
    }

    public static boolean isAbstract(Member member) {
        return Modifier.isAbstract(member.getModifiers());
    }

    public static boolean isNotAbstract(Member member) {
        return !isAbstract(member);
    }

    public static boolean isFinal(Member member) {
        return Modifier.isFinal(member.getModifiers());
    }

    public static boolean isNotFinal(Member member) {
        return !isFinal(member);
    }

    public static boolean isNative(Member member) {
        return Modifier.isNative(member.getModifiers());
    }

    public static boolean isNotNative(Member member) {
        return !isNative(member);
    }

    public static boolean isVolatile(Member member) {
        return Modifier.isVolatile(member.getModifiers());
    }

    public static boolean isNotVolatile(Member member) {
        return !isVolatile(member);
    }

    public static boolean isSynchronized(Member member) {
        return Modifier.isSynchronized(member.getModifiers());
    }

    public static boolean isNotSynchronized(Member member) {
        return !isSynchronized(member);
    }

    public static boolean isTransient(Member member) {
        return Modifier.isTransient(member.getModifiers());
    }

    public static boolean isNotTransient(Member member) {
        return !isTransient(member);
    }

}
