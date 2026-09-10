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

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class DelayQueuePublishExceptionTest {

    @Test
    void carriesMessageOnly() {
        var e = new DelayQueuePublishException("boom");
        assertThat(e).isInstanceOf(RuntimeException.class);
        assertThat(e.getMessage()).isEqualTo("boom");
        assertThat(e.getCause()).isNull();
    }

    @Test
    void carriesMessageAndCause() {
        var cause = new IOException("root");
        var e = new DelayQueuePublishException("boom", cause);
        assertThat(e.getMessage()).isEqualTo("boom");
        assertThat(e.getCause()).isSameAs(cause);
    }
}
