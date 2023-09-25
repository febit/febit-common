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
package org.febit.lang.pubsub;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriberManagerTest {

    static Predicate<Object> isSameClass(Class<?> cls) {
        return s -> s.getClass() == cls;
    }

    @Test
    void resolve() {

        var mgr = SubscriberManager.create(List.of(
                new FooSub(),
                new FooExactlySub(),
                new FooBarSub(),
                new FooBarSub2(),
                new StringSub()
        ));

        assertThat(mgr.resolve(new Object()))
                .isEmpty();

        assertThat(mgr.resolve("string"))
                .hasSize(1)
                .anyMatch(isSameClass(StringSub.class))
        ;

        assertThat(mgr.resolve(new Foo()))
                .hasSize(2)
                .anyMatch(isSameClass(FooSub.class))
                .anyMatch(isSameClass(FooExactlySub.class))
        ;

        assertThat(mgr.resolve(new Foo2()))
                .hasSize(1)
                .anyMatch(isSameClass(FooSub.class))
        ;

        assertThat(mgr.resolve(new FooBar()))
                .hasSize(3)
                .anyMatch(isSameClass(FooSub.class))
                .anyMatch(isSameClass(FooBarSub.class))
                .anyMatch(isSameClass(FooBarSub2.class))
        ;
    }

    interface IFoo {

    }

    interface IBar {

    }

    static class Foo implements IFoo {

    }

    static class Foo2 implements IFoo {

    }

    static class FooBar implements IFoo, IBar {
    }

    static class FooSub implements ISubscriber<IFoo> {
    }

    static class FooExactlySub implements ISubscriber<Foo> {
    }

    static class FooBarSub implements ISubscriber<FooBar> {
    }

    static class FooBarSub2 extends FooBarSub {
    }

    static class StringSub implements ISubscriber<String> {
    }
}
