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
package org.febit.common.caffeine;

import org.febit.lang.NanosClock;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A controllable clock for testing.
 * Supports setting time and registering change listeners.
 */
public class ControllableClock implements NanosClock {

    private final List<Runnable> onChangedListeners = new ArrayList<>();
    private long nanos;

    public ControllableClock(long nanos) {
        this.nanos = nanos;
    }

    @Override
    public long now() {
        return nanos;
    }

    /**
     * Set the current time.
     */
    public void set(long nanos) {
        this.nanos = nanos;
        notifyChanged();
    }

    /**
     * Advance time by the given duration.
     */
    public void advance(long amount, TimeUnit unit) {
        set(nanos + unit.toNanos(amount));
    }

    /**
     * Advance time by the given duration.
     */
    public void advance(Duration duration) {
        set(nanos + duration.toNanos());
    }

    /**
     * Register a listener that will be called whenever the clock changes.
     */
    public void onChange(Runnable listener) {
        onChangedListeners.add(listener);
    }

    private void notifyChanged() {
        for (Runnable listener : onChangedListeners) {
            listener.run();
        }
    }
}
