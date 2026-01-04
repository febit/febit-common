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
package org.febit.lang;

import org.jspecify.annotations.NullMarked;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@NullMarked
public interface TimeDelayed extends Delayed {

    long getTimeInMillis();

    @Override
    default long getDelay(TimeUnit unit) {
        var now = currentTimeMillis();
        return unit.convert(getTimeInMillis() - now, TimeUnit.MILLISECONDS);
    }

    default long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    default int compareTo(Delayed other) {
        if (other == this) {
            return 0;
        }
        if (other instanceof TimeDelayed) {
            return Long.compare(getTimeInMillis(), ((TimeDelayed) other).getTimeInMillis());
        }
        return Long.compare(
                getDelay(TimeUnit.NANOSECONDS),
                other.getDelay(TimeUnit.NANOSECONDS)
        );
    }

}
