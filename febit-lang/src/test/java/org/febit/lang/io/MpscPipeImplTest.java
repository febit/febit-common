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
package org.febit.lang.io;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MpscPipeImplTest {

    record Stub(int worker, int round) {
    }

    @Test
    void async() {
        var n = 5;
        var rounds = 10;

        var executor = Executors.newFixedThreadPool(n + 1);
        var pipe = MpscPipeImpl.<Stub>ofBounded(1);

        var accepted = ConcurrentHashMap.<Stub>newKeySet();
        var futures = new ArrayList<Future<?>>();
        futures.add(executor.submit(() -> {
            pipe.stream().forEach(stub -> {
                log.info("Got: {}", stub);
                accepted.add(stub);
            });
        }));

        for (int i = 0; i < n; i++) {
            var worker = i;
            futures.add(executor.submit(() -> {
                try (var producer = pipe.createProducer()) {
                    for (int round = 0; round < rounds; round++) {
                        var stub = new Stub(worker, round);
                        producer.accept(stub);
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        futures.forEach(future -> {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();

        for (int i = 0; i < n; i++) {
            for (int round = 0; round < rounds; round++) {
                var stub = new Stub(i, round);
                assertTrue(accepted.contains(stub), "Expected to find: " + stub);
            }
        }

    }

}
