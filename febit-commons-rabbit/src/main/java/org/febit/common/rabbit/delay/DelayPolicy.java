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

import java.time.Duration;

/**
 * Decides how long a message waits before its next hop.
 *
 * <p>Implementations must be thread-safe.  The default rounds the remaining time up to the
 * next whole second, so that messages share TTL tiers instead of one queue per delay.
 */
public interface DelayPolicy {

    /**
     * @param ctx current message context
     * @return wait time for the next hop; {@code <= 0} means the message is due now
     */
    Duration delay(DelayContext ctx);
}
