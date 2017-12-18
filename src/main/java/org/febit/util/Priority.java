/**
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
package org.febit.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author zqq90
 */
public class Priority {

    public static final int PRI_HIGHEST = 1 << 30;
    public static final int PRI_HIGHER = 1 << 16;
    public static final int PRI_HIGH = 1 << 14;
    public static final int PRI_NORMAL = 1 << 12;
    public static final int PRI_LOW = 1 << 10;
    public static final int PRI_LOWER = 1 << 8;
    public static final int PRI_LOWEST = 0;

    public static final Comparator<Object> ASC = (o1, o2) -> Integer.compare(getPriority(o1), getPriority(o2));
    public static final Comparator<Object> DESC = ASC.reversed();

    public static <T> void asc(T[] array) {
        Arrays.sort(array, ASC);
    }

    public static <T> void desc(T[] array) {
        Arrays.sort(array, DESC);
    }

    public static <T> void asc(List<T> list) {
        Collections.sort(list, ASC);
    }

    public static <T> void desc(List<T> list) {
        Collections.sort(list, DESC);
    }

    protected static int getPriority(Level level) {
        if (level == null) {
            return PRI_NORMAL;
        }
        return level.value();
    }

    protected static int getPriority(Class<?> type) {
        Level priority = type.getAnnotation(Level.class);
        if (priority != null) {
            return getPriority(priority);
        }
        for (Annotation annotation : type.getAnnotations()) {
            priority = annotation.annotationType().getAnnotation(Level.class);
            if (priority != null) {
                return getPriority(priority);
            }
        }
        return PRI_NORMAL;
    }

    protected static int getPriority(Object bean) {
        if (bean == null) {
            return PRI_LOWEST;
        }
        if (bean instanceof Class) {
            return getPriority((Class) bean);
        }
        return getPriority(bean.getClass());
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    public static @interface Level {

        int value() default PRI_NORMAL;
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Level(PRI_HIGHEST)
    public static @interface Highest {
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Level(PRI_HIGHER)
    public static @interface Higher {
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Level(PRI_HIGH)
    public static @interface High {
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Level(PRI_NORMAL)
    public static @interface Normal {
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Level(PRI_LOW)
    public static @interface Low {
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Level(PRI_LOWER)
    public static @interface Lower {
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Level(PRI_LOWEST)
    public static @interface Lowest {
    }
}
