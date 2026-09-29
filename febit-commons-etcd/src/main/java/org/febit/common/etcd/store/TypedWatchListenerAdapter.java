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

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.watch.WatchResponse;

import org.febit.common.etcd.store.codec.KVCodec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@lombok.RequiredArgsConstructor(staticName = "of")
class TypedWatchListenerAdapter<K, V> implements Watch.Listener {

    private final ByteSequence target;
    private final KVCodec<K, V> codec;
    private final TypedWatchObserver<K, V> observer;
    private final EtcdMetrics metrics;

    @Override
    public void onNext(WatchResponse response) {
        for (var event : response.getEvents()) {
            try {
                var key = codec.key().decode(event.getKeyValue().getKey());
                if (key == null) {
                    continue;
                }
                var prevValue = codec.decodeValue(event.getPrevKV());
                var nextValue = codec.decodeValue(event.getKeyValue());
                observer.onEvent(key, prevValue, nextValue, event.getEventType(), event.getKeyValue().getModRevision());
            } catch (Exception e) {
                log.error("[Etcd] Error handling watch event", e);
                metrics.error().record(e);
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("[Etcd] Watch error on target={}", target, throwable);
        metrics.watch().reconnect();
        observer.onError(throwable);
        // A reconnect can silently drop events; the caller must reconcile from the truth store afterwards.
        observer.onReconnected();
    }

    @Override
    public void onCompleted() {
        log.info("[Etcd] Watch completed on target={}", target);
        metrics.watch().completed();
        observer.onCompleted();
    }
}
