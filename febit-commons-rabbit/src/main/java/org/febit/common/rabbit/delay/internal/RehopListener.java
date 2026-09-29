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

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

import org.febit.common.rabbit.delay.DelayContext;
import org.febit.common.rabbit.delay.DelayQueueOptions;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

@Slf4j
public final class RehopListener implements ChannelAwareMessageListener {

    private final DelayQueueOptions options;
    private final Dispatcher dispatcher;

    public RehopListener(DelayQueueOptions options) {
        this.options = options;
        this.dispatcher = new Dispatcher(options);
    }

    private static void ack(Channel ch, long deliveryTag) {
        try {
            ch.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("[Delay] rehop ack failed tag={}", deliveryTag, e);
        }
    }

    private static void nack(Channel ch, long deliveryTag) {
        try {
            ch.basicNack(deliveryTag, false, false);
        } catch (IOException e) {
            log.error("[Delay] rehop nack failed tag={}", deliveryTag, e);
        }
    }

    @Override
    public void onMessage(Message msg, @Nullable Channel ch) {
        Objects.requireNonNull(ch, "channel");

        long deliveryTag = msg.getMessageProperties().getDeliveryTag();
        var control = DelayContext.Control.from(msg);
        if (control == null) {
            log.error("[Delay] rehop dropped: malformed control headers tag={}", deliveryTag);
            options.metrics().onDeadLetter(null, -1, "parse-failed");
            nack(ch, deliveryTag);
            return;
        }
        var ctx = new DelayContext(options, control, DelayContext.Envelope.from(msg));

        boolean confirmed;
        try {
            confirmed = dispatcher.dispatch(ctx);
        } catch (IOException e) {
            log.warn("[Delay] rehop publish failed id={} reason={}", ctx.control().id(), e.getMessage());
            confirmed = false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            confirmed = false;
        } catch (TimeoutException e) {
            log.warn("[Delay] rehop publish timeout id={}", ctx.control().id());
            confirmed = false;
        }

        if (!confirmed) {
            reenter(ch, ctx, deliveryTag);
            return;
        }
        ack(ch, deliveryTag);
    }

    private void reenter(Channel ch, DelayContext ctx, long deliveryTag) {
        if (log.isDebugEnabled()) {
            log.debug("[Delay] rehop re-entered after dispatch failed id={} rk={} attempts={}",
                    ctx.control().id(), ctx.control().routingKey(), ctx.control().attempts());
        }
        boolean confirmed;
        try {
            confirmed = dispatcher.reenter(ctx);
        } catch (IOException e) {
            log.warn("[Delay] rehop re-enter publish failed id={} reason={}", ctx.control().id(), e.getMessage());
            confirmed = false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            confirmed = false;
        } catch (TimeoutException e) {
            log.warn("[Delay] rehop re-enter timeout id={}", ctx.control().id());
            confirmed = false;
        }
        if (confirmed) {
            ack(ch, deliveryTag);
        } else {
            log.error("[Delay] rehop re-enter failed id={}, nack without requeue (routed to DLQ)",
                    ctx.control().id());
            nack(ch, deliveryTag);
        }
    }
}
