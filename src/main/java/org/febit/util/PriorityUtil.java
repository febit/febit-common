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
public class PriorityUtil {

    public static final int PRI_HIGHEST = 1 << 30;
    public static final int PRI_HIGHER = 1 << 16;
    public static final int PRI_HIGH = 1 << 14;
    public static final int PRI_NORMAL = 1 << 12;
    public static final int PRI_LOW = 1 << 10;
    public static final int PRI_LOWER = 1 << 8;
    public static final int PRI_LOWEST = 0;

    protected static Comparator ASC = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            return Integer.compare(getPriority(o1), getPriority(o2));
        }
    };

    protected static Comparator DESC = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            return Integer.compare(getPriority(o2), getPriority(o1));
        }
    };

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

    protected static int getPriority(Priority priority) {
        if (priority == null) {
            return PRI_NORMAL;
        }
        return priority.value();
    }

    protected static int getPriority(Class<?> type) {
        Priority priority = type.getAnnotation(Priority.class);
        if (priority != null) {
            return getPriority(priority);
        }
        for (Annotation annotation : type.getAnnotations()) {
            priority = annotation.annotationType().getAnnotation(Priority.class);
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
    public static @interface Priority {

        int value() default PRI_NORMAL;
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Priority(PRI_HIGHEST)
    public static @interface Highest {
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Priority(PRI_HIGHER)
    public static @interface Higher {
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Priority(PRI_HIGH)
    public static @interface High {
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Priority(PRI_NORMAL)
    public static @interface Normal {
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Priority(PRI_LOW)
    public static @interface Low {
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Priority(PRI_LOWER)
    public static @interface Lower {
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Priority(PRI_LOWEST)
    public static @interface Lowest {
    }
}
