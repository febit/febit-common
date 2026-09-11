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

import io.etcd.jetcd.watch.WatchEvent;
import org.jspecify.annotations.Nullable;

/**
 * Observer for decoded etcd watch events. Key and value are already typed objects.
 *
 * @param <K> key type
 * @param <V> value type
 */
@FunctionalInterface
public interface TypedWatchObserver<K, V> {

    /**
     * @param revision etcd mod_revision of the event; carried for free with the watch event,
     *                 so prefer it over a fresh GET when gating a projection revision
     */
    void onEvent(K key, @Nullable V prev, @Nullable V next, WatchEvent.EventType eventType, long revision);

    default void onError(Throwable t) {
    }

    default void onCompleted() {
    }

    /**
     * Invoked after a watch reconnects (about to be rebuilt); callers may trigger a full reconcile.
     * Default empty to avoid breaking existing observers.
     */
    default void onReconnected() {
    }
}
