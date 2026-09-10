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

import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaffeineThrottleTest {

    private static final Duration WINDOW = Duration.ofSeconds(15);
    private static final long WINDOW_NANOS = WINDOW.toNanos();
    private static final long BASE_NANOS = 1_000_000_000L;

    private static CaffeineThrottle<String> throttle(AtomicLong clock) {
        return CaffeineThrottle.create(WINDOW,
                Caffeine.newBuilder()
                        .expireAfterAccess(WINDOW.multipliedBy(4))
                        .maximumSize(100),
                clock::get);
    }

    private static AtomicLong newClock() {
        return new AtomicLong(BASE_NANOS);
    }

    @Nested
    class Execute {

        @Test
        void passesFirstCall() {
            var clock = newClock();
            var t = throttle(clock);
            var counter = new AtomicInteger();

            var ok = t.execute("a", counter::incrementAndGet);

            assertThat(ok).isTrue();
            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        void throttlesWithinWindow() {
            var clock = newClock();
            var t = throttle(clock);
            var counter = new AtomicInteger();

            var first = t.execute("a", counter::incrementAndGet);
            clock.addAndGet(WINDOW_NANOS / 2);
            var second = t.execute("a", counter::incrementAndGet);

            assertThat(first).isTrue();
            assertThat(second).isFalse();
            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        void passesAfterWindowExpires() {
            var clock = newClock();
            var t = throttle(clock);
            var counter = new AtomicInteger();

            var first = t.execute("a", counter::incrementAndGet);
            clock.addAndGet(WINDOW_NANOS + 1);
            var second = t.execute("a", counter::incrementAndGet);

            assertThat(first).isTrue();
            assertThat(second).isTrue();
            assertThat(counter.get()).isEqualTo(2);
        }

        @Test
        void passesExactlyAtWindowBoundary() {
            var clock = newClock();
            var t = throttle(clock);
            var counter = new AtomicInteger();

            var first = t.execute("a", counter::incrementAndGet);
            clock.addAndGet(WINDOW_NANOS);
            var second = t.execute("a", counter::incrementAndGet);

            assertThat(first).isTrue();
            assertThat(second).isTrue();
            assertThat(counter.get()).isEqualTo(2);
        }

        @Test
        void throttledCallDoesNotExtendWindow() {
            var clock = newClock();
            var t = throttle(clock);
            var counter = new AtomicInteger();

            var e1 = t.execute("a", counter::incrementAndGet);  // t=0
            clock.addAndGet(WINDOW_NANOS / 3);
            var e2 = t.execute("a", counter::incrementAndGet);  // t=W/3, throttled
            clock.addAndGet(WINDOW_NANOS / 3);
            var e3 = t.execute("a", counter::incrementAndGet);  // t=2W/3, still throttled
            clock.addAndGet(WINDOW_NANOS / 3);
            var e4 = t.execute("a", counter::incrementAndGet);  // t=W, passes

            assertThat(e1).isTrue();
            assertThat(e2).isFalse();
            assertThat(e3).isFalse();
            assertThat(e4).isTrue();
            assertThat(counter.get()).isEqualTo(2);
        }

        @Test
        void differentKeysDoNotInterfere() {
            var clock = newClock();
            var t = throttle(clock);
            var a = new AtomicInteger();
            var b = new AtomicInteger();

            t.execute("a", a::incrementAndGet);
            t.execute("b", b::incrementAndGet);
            clock.addAndGet(WINDOW_NANOS / 2);
            var a2 = t.execute("a", a::incrementAndGet);  // throttled
            var b2 = t.execute("b", b::incrementAndGet);  // throttled

            assertThat(a2).isFalse();
            assertThat(b2).isFalse();
            assertThat(a.get()).isEqualTo(1);
            assertThat(b.get()).isEqualTo(1);
        }

        @Test
        void propagatesRuntimeException() {
            var clock = newClock();
            var t = throttle(clock);

            assertThatThrownBy(() -> t.execute("a", () -> {
                throw new IllegalArgumentException("boom");
            })).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("boom");
        }

        @Test
        void propagatesCheckedException() {
            var clock = newClock();
            var t = throttle(clock);

            assertThatThrownBy(() -> t.execute("a", () -> {
                throw new Exception("checked");
            })).isInstanceOf(Exception.class)
                    .hasMessage("checked");
        }

        @Test
        void marksActionOnExceptionToPreventRetryStorm() {
            var clock = newClock();
            var t = throttle(clock);

            assertThatCode(() -> t.execute("a", () -> {
                throw new RuntimeException("fail");
            })).isInstanceOf(RuntimeException.class);

            // Should be throttled — exception still marks the key
            var ok = t.execute("a", () -> {
            });
            assertThat(ok).isFalse();
        }

        @Test
        void exceptionDoesNotThrottleDifferentKey() {
            var clock = newClock();
            var t = throttle(clock);

            assertThatCode(() -> t.execute("a", () -> {
                throw new RuntimeException("fail");
            })).isInstanceOf(RuntimeException.class);

            // Key "b" is unaffected by "a"'s exception
            var ok = t.execute("b", () -> {
            });
            assertThat(ok).isTrue();
        }

        @Test
        void exceptionThrottleExpiresAfterWindow() {
            var clock = newClock();
            var t = throttle(clock);

            assertThatCode(() -> t.execute("a", () -> {
                throw new RuntimeException("fail");
            })).isInstanceOf(RuntimeException.class);

            clock.addAndGet(WINDOW_NANOS + 1);
            var ok = t.execute("a", () -> {
            });
            assertThat(ok).isTrue();
        }
    }

    @Nested
    class Supply {

        @Test
        void returnsResult() {
            var clock = newClock();
            var t = throttle(clock);

            var result = t.supply("a", () -> 42);

            assertThat(result).hasValue(42);
        }

        @Test
        void returnsEmptyWhenThrottled() {
            var clock = newClock();
            var t = throttle(clock);

            t.supply("a", () -> 1);
            clock.addAndGet(WINDOW_NANOS / 2);
            var result = t.supply("a", () -> 2);

            assertThat(result).isEmpty();
        }

        @Test
        void supplierReturningNullDoesNotThrowNpe() {
            var clock = newClock();
            var t = throttle(clock);

            // Supplier returns null → Optional.empty(), no NPE
            var result = t.supply("a", () -> null);
            assertThat(result).isEmpty();
        }

        @Test
        void nullResultAndThrottledBothReturnEmpty() {
            var clock = newClock();
            var t = throttle(clock);

            // Supplier returned null → Optional.empty()
            var first = t.supply("a", () -> null);
            assertThat(first).isEmpty();

            // Within window → Optional.empty() (throttled)
            var second = t.supply("a", () -> 42);
            assertThat(second).isEmpty();

            // After window expires → Optional.empty() (supplier null again)
            clock.addAndGet(WINDOW_NANOS + 1);
            var third = t.supply("a", () -> null);
            assertThat(third).isEmpty();
        }

        @Test
        void propagatesException() {
            var clock = newClock();
            var t = throttle(clock);

            assertThatThrownBy(() -> t.supply("a", () -> {
                throw new IllegalStateException("bad");
            })).isInstanceOf(IllegalStateException.class)
                    .hasMessage("bad");
        }

        @Test
        void marksActionOnExceptionToPreventRetryStorm() {
            var clock = newClock();
            var t = throttle(clock);

            assertThatCode(() -> t.supply("a", () -> {
                throw new RuntimeException("fail");
            })).isInstanceOf(RuntimeException.class);

            // Should be throttled — exception still marks the key
            assertThat(t.supply("a", () -> 42)).isEmpty();
        }
    }

    @Nested
    class Touch {

        @Test
        void extendsThrottleWindow() {
            var clock = newClock();
            var t = throttle(clock);

            t.execute("a", () -> {
            });
            // Advance to just before window expires
            clock.addAndGet(WINDOW_NANOS - 1);
            t.touch("a");
            // Window should now extend from touch time
            var counter = new AtomicInteger();
            t.execute("a", counter::incrementAndGet);

            assertThat(counter.get()).isZero();
        }

        @Test
        void noOpForAbsentKey() {
            var clock = newClock();
            var t = throttle(clock);

            assertThatCode(() -> t.touch("nonexistent")).doesNotThrowAnyException();
        }

        @Test
        void touchDoesNotCreateSlot() {
            var clock = newClock();
            var t = throttle(clock);

            t.touch("absent");
            // Should still be absent — touch shouldn't create
            var counter = new AtomicInteger();
            t.execute("absent", counter::incrementAndGet);
            assertThat(counter.get()).isEqualTo(1);
        }
    }

    @Nested
    class Reset {

        @Test
        void clearsThrottleState() {
            var clock = newClock();
            var t = throttle(clock);
            var counter = new AtomicInteger();

            t.execute("a", counter::incrementAndGet);
            t.reset("a");
            // Reset clears state, so a fresh execute passes immediately — no time advance needed.
            t.execute("a", counter::incrementAndGet);

            assertThat(counter.get()).isEqualTo(2);
        }

        @Test
        void noOpForAbsentKey() {
            var clock = newClock();
            var t = throttle(clock);

            assertThatCode(() -> t.reset("nonexistent")).doesNotThrowAnyException();
        }
    }

    @Nested
    class EstimatedSize {

        @Test
        void startsAtZero() {
            var clock = newClock();
            var t = throttle(clock);

            assertThat(t.estimatedSize()).isZero();
        }

        @Test
        void growsWithNewKeys() {
            var clock = newClock();
            var t = throttle(clock);

            t.execute("a", () -> {
            });
            assertThat(t.estimatedSize()).isPositive();

            t.execute("b", () -> {
            });
            assertThat(t.estimatedSize()).isGreaterThanOrEqualTo(2);
        }

        @Test
        void sameKeyDoesNotIncreaseSize() {
            var clock = newClock();
            var t = throttle(clock);

            t.execute("a", () -> {
            });
            var size1 = t.estimatedSize();
            clock.addAndGet(WINDOW_NANOS + 1);
            t.execute("a", () -> {
            });
            var size2 = t.estimatedSize();

            assertThat(size2).isEqualTo(size1);
        }

        @Test
        void dropsToZeroAfterReset() {
            var clock = newClock();
            var t = throttle(clock);

            t.execute("a", () -> {
            });
            assertThat(t.estimatedSize()).isPositive();

            t.reset("a");
            assertThat(t.estimatedSize()).isZero();
        }
    }

    @Nested
    class Concurrency {

        @Test
        void mutexSerializesSameKey() throws Exception {
            var clock = newClock();
            var t = throttle(clock);
            var inside = new CountDownLatch(1);
            var entered = new CountDownLatch(1);
            var bActionRan = new AtomicInteger();
            var bExited = new AtomicBoolean();

            // Thread A: holds the mutex
            var threadA = new Thread(() -> {
                t.execute("a", () -> {
                    entered.countDown();
                    try {
                        inside.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            });
            threadA.start();
            entered.await();

            // Thread B: tries same key while A holds mutex — should block
            var threadB = new Thread(() -> {
                t.execute("a", bActionRan::incrementAndGet);
                bExited.set(true);
            });
            threadB.start();

            // Give B time to reach the mutex
            Thread.sleep(200);
            // A is still inside, B should be blocked
            assertThat(bActionRan.get()).isZero();

            // Release A
            inside.countDown();
            threadA.join();
            threadB.join();

            // B entered the mutex (proved by exited flag);
            // action was throttled because window didn't expire
            assertThat(bExited.get()).isTrue();
            assertThat(bActionRan.get()).isZero();
        }

        @Test
        void differentKeysRunConcurrently() throws Exception {
            var clock = newClock();
            var t = throttle(clock);
            var latch = new CountDownLatch(2);
            var bothEntered = new CountDownLatch(2);

            var threadA = new Thread(() -> {
                t.execute("a", () -> {
                    bothEntered.countDown();
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            });
            var threadB = new Thread(() -> {
                t.execute("b", () -> {
                    bothEntered.countDown();
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            });
            threadA.start();
            threadB.start();

            // Both should enter concurrently (different keys = different slots)
            bothEntered.await();
            latch.countDown();
            latch.countDown();
            threadA.join();
            threadB.join();
        }
    }
}
