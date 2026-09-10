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

import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Send-side facade for delayed message delivery over RabbitMQ.
 *
 * <p>A message offered here is parked in an internal {@code wait} queue with a
 * per-message TTL; when the TTL expires it is re-evaluated and either parked again
 * (re-hop) or published to the {@linkplain #readyExchange() ready exchange}, where the
 * user's own consumers pick it up.  All re-hopping is handled internally — callers only
 * ever see {@code offer(...)} and the resulting {@link DelayReceipt}.
 *
 * <p>Implementations are thread-safe.
 *
 * <h3>End-to-end usage</h3>
 * <pre>{@code
 * // 1. ready exchange — declared by the application
 * @Bean
 * TopicExchange readyExchange() {
 *     return new TopicExchange("jobs.ready.ex", true, false);
 * }
 *
 * // 2. delay queue
 * @Bean
 * RabbitDelayQueue delayQueue(ConnectionFactory cf) {
 *     return DelayQueueOptions.builder()
 *             .name("jobs")
 *             .connectionFactory(cf)
 *             .readyExchange("jobs.ready.ex")
 *             .build()
 *             .createQueue();
 * }
 *
 * // 3. bind a consumer queue to the ready exchange
 * @Bean
 * Declarables jobTopology(TopicExchange readyExchange) {
 *     var queue = QueueBuilder.durable("jobs.q").build();
 *     var binding = BindingBuilder.bind(queue).to(readyExchange).with("jobs.run");
 *     return new Declarables(List.of(queue, binding));
 * }
 *
 * // 4. schedule
 * try {
 *     delayQueue.offer(DelayMessage.builder()
 *             .routingKey("jobs.run")
 *             .jsonContent(payload)
 *             .deadline(until)
 *             .build());
 * } catch (DelayQueuePublishException e) {
 *     // nothing is armed — handle the gap, no trigger will fire
 * }
 *
 * // 5. consume, once the deadline passes
 * @RabbitListener(queues = "jobs.q")
 * void on(Message message) { ... }
 * }</pre>
 *
 * <p>The ready exchange and its bindings are declared by the application, not by the queue.
 *
 * <p>Overloads differ only in addressing: {@code offer*} takes a relative {@code delay},
 * {@code offerAt*} an absolute {@code deadline}; omitting the routing key falls back to the
 * queue default.  See the package javadoc for end-to-end usage.
 */
public interface RabbitDelayQueue extends AutoCloseable {

    String readyExchange();

    ConnectionFactory connectionFactory();

    /**
     * Release listeners and connections; further {@code offer(...)} calls fail.
     */
    @Override
    void close();

    /**
     * @throws DelayQueuePublishException if it cannot be published; nothing is scheduled then
     */
    DelayReceipt offer(DelayMessage msg);

    default DelayReceipt offer(String routingKey, String body, Duration delay) {
        return offer(DelayMessage.builder()
                .routingKey(routingKey)
                .delay(delay)
                .content(body)
                .build());
    }

    default DelayReceipt offerAt(String routingKey, Map<String, Object> headers, String body, Instant deadline) {
        return offer(DelayMessage.builder()
                .routingKey(routingKey)
                .headers(headers)
                .deadline(deadline)
                .content(body)
                .build());
    }

    default DelayReceipt offer(String body, Duration delay) {
        return offer(DelayMessage.builder()
                .delay(delay)
                .content(body)
                .build());
    }

    default DelayReceipt offerAt(Map<String, Object> headers, String body, Instant deadline) {
        return offer(DelayMessage.builder()
                .headers(headers)
                .deadline(deadline)
                .content(body)
                .build());
    }
}
