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
package org.febit.vtor;

import java.lang.annotation.Annotation;

/**
 * @param <T>
 * @author zqq90
 */
public interface Check<T extends Annotation> {

    /**
     * Do check.
     *
     * @param anno
     * @param value
     * @return null if pass.
     */
    Object[] check(T anno, Object value);

    /**
     * Get default message.
     *
     * @param result valid results
     * @return
     */
    String getDefaultMessage(Object[] result);
}
