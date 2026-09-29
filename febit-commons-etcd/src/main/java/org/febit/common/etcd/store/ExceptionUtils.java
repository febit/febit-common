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
package org.febit.common.etcd.store;

import org.febit.lang.UncheckedException;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ExecutionException;

/**
 * Unwraps etcd execution exceptions into a caller-friendly {@link RuntimeException}.
 */
@UtilityClass
public class ExceptionUtils {

    /**
     * Unwraps the cause of an {@link ExecutionException}, restores the interrupt flag for an
     * {@link InterruptedException}, rethrows any {@link RuntimeException} as-is, and otherwise
     * wraps the cause in an {@link org.febit.lang.UncheckedException}.
     *
     * @param message context message for the wrapped exception
     */
    public static RuntimeException unwrap(Throwable e, String message) {
        while (e instanceof ExecutionException && e.getCause() != null) {
            e = e.getCause();
        }
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        if (e instanceof RuntimeException re) {
            return re;
        }
        return new UncheckedException(message, e);
    }
}
