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
package org.febit.common.rabbit.delay;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DelayQueueTopologyTest {

    @Nested
    class DefaultNaming {

        @Test
        void derivesResourcesFromNamespace() {
            var topology = new DelayQueueTopology("ns", null, null);

            assertThat(topology.waitQueue().queue()).isEqualTo("ns.wait.q");
            assertThat(topology.ready().exchange()).isEqualTo("ns.ready.ex");
            assertThat(topology.ready().routingKey()).isNull();
            assertThat(topology.rehop().exchange()).isEqualTo("ns.rehop.ex");
            assertThat(topology.rehop().queue()).isEqualTo("ns.rehop.q");
            assertThat(topology.dlx().exchange()).isEqualTo("ns.rehop.dlx");
            assertThat(topology.dlx().queue()).isEqualTo("ns.rehop.dlq");
        }
    }

    @Nested
    class CustomNaming {

        @Test
        void honorsReadyExchangeAndRoutingKeyOverride() {
            var topology = new DelayQueueTopology("ns", "custom.ready.ex", "rk1");

            assertThat(topology.ready().exchange()).isEqualTo("custom.ready.ex");
            assertThat(topology.ready().routingKey()).isEqualTo("rk1");
            assertThat(topology.rehop().exchange()).isEqualTo("ns.rehop.ex");
            assertThat(topology.dlx().queue()).isEqualTo("ns.rehop.dlq");
        }
    }

    @Nested
    class Validation {

        @Test
        void rejectsBlankNamespace() {
            assertThatThrownBy(() -> new DelayQueueTopology(" ", null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("namespace");
        }
    }
}
