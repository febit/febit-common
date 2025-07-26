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
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Slf4j
@UtilityClass
public class ExecutorUtils {

    private static final AtomicLong REJECTED_COUNT = new AtomicLong(0L);

    public static ThreadFactory threadFactory(String prefix) {
        var seq = new AtomicLong(1);
        return run -> new Thread(run, prefix + seq.getAndIncrement());
    }

    public static RejectedExecutionHandler blockingRejected(Duration timeout, Consumer<Runnable> onFailed) {
        return (run, exec) -> {
            boolean ok;
            try {
                ok = exec.getQueue().offer(run, timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                var count = REJECTED_COUNT.updateAndGet(c -> c == Long.MAX_VALUE ? Long.MAX_VALUE : c + 1);
                if (log.isDebugEnabled()) {
                    log.debug("Task {} rejected from {}. total rejected: {}", run, e, count, e);
                }
                ok = false;
            }
            if (!ok) {
                onFailed.accept(run);
            }
        };
    }
}
