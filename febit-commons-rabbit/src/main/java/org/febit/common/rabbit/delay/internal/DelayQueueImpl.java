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
package org.febit.common.rabbit.delay.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.febit.common.rabbit.delay.DelayContext;
import org.febit.common.rabbit.delay.DelayMessage;
import org.febit.common.rabbit.delay.DelayQueueOptions;
import org.febit.common.rabbit.delay.DelayQueuePublishException;
import org.febit.common.rabbit.delay.DelayReceipt;
import org.febit.common.rabbit.delay.Headers;
import org.febit.common.rabbit.delay.RabbitDelayQueue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.febit.lang.util.Defaults.collapse;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DelayQueueImpl implements RabbitDelayQueue {

    private final DelayQueueOptions options;
    private final SimpleMessageListenerContainer container;
    private final Dispatcher dispatcher;

    public static DelayQueueImpl create(
            DelayQueueOptions options,
            SimpleMessageListenerContainer container
    ) {
        var dispatcher = new Dispatcher(options);
        return new DelayQueueImpl(options, container, dispatcher);
    }

    @Override
    public String readyExchange() {
        return options.topology().ready().exchange();
    }

    @Override
    public ConnectionFactory connectionFactory() {
        return options.connectionFactory();
    }

    @Override
    public void close() {
        if (container.isRunning()) {
            container.shutdown();
        }
        log.info("[Delay] queue closed name={}", options.name());
    }

    @Override
    public DelayReceipt offer(DelayMessage msg) {
        var ctx = resolve(msg);
        dispatch(ctx);
        var control = ctx.control();
        options.metrics().onOffer(control.id(), control.routingKey());
        return new DelayReceipt(
                control.id(),
                control.routingKey(),
                control.deadline(),
                control.total()
        );
    }

    private DelayContext resolve(DelayMessage msg) {
        var routingKey = collapse(msg.routingKey(), options.topology().ready().routingKey());
        if (StringUtils.isEmpty(routingKey)) {
            throw new IllegalArgumentException(
                    "delay message requires a routingKey (neither message nor options.defaultRoutingKey provided)");
        }
        Headers.validateUserHeaders(msg.headers());

        var now = options.clock().instant();
        var deadline = msg.deadline();

        var total = Duration.between(now, deadline);
        if (total.isNegative()) {
            total = Duration.ZERO;
        }
        var id = options.idGenerator().next(msg);
        var control = DelayContext.Control.builder()
                .id(id)
                .attempts(0)
                .deadline(deadline)
                .total(total)
                .routingKey(routingKey)
                .build();
        var envelope = DelayContext.Envelope.builder()
                .headers(msg.headers())
                .body(msg.content())
                .contentType(msg.contentType())
                .contentEncoding(msg.contentEncoding())
                .build();
        return DelayContext.of(options, control, envelope);
    }

    private void dispatch(DelayContext ctx) {
        try {
            if (!dispatcher.dispatch(ctx)) {
                throw new DelayQueuePublishException("broker confirm timeout: id=" + ctx.control().id());
            }
        } catch (IOException e) {
            throw new DelayQueuePublishException("failed to publish: id=" + ctx.control().id(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DelayQueuePublishException("interrupted while waiting for confirm: id=" + ctx.control().id(), e);
        } catch (TimeoutException e) {
            throw new DelayQueuePublishException("broker confirm wait timed out: id=" + ctx.control().id(), e);
        }
    }
}
