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

import com.rabbitmq.client.AMQP;

import org.febit.common.rabbit.delay.DelayContext;
import org.febit.common.rabbit.delay.DelayQueueOptions;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Decides the next hop for a {@link DelayContext}, publishes it and waits for the confirm.
 */
@Slf4j
@lombok.RequiredArgsConstructor
final class Dispatcher {

    private static final List<DelayTier> TIERS = List.of(
            new DelayTier(1, 19),
            new DelayTier(2, 18),
            new DelayTier(5, 17),
            new DelayTier(15, 16),
            new DelayTier(30, 15),
            new DelayTier(60, 14),
            new DelayTier(120, 13),
            new DelayTier(300, 12),
            new DelayTier(900, 11),
            new DelayTier(1800, 10),
            new DelayTier(3600, 9),
            new DelayTier(10_800, 8),
            new DelayTier(21_600, 7),
            new DelayTier(43_200, 6)
    );

    private final DelayQueueOptions options;

    private static DelayTier selectTier(Duration wait) {
        var waitSeconds = wait.toSeconds();
        var picked = TIERS.getFirst();
        for (var tier : TIERS) {
            if (tier.seconds() <= waitSeconds) {
                picked = tier;
            } else {
                break;
            }
        }
        return picked;
    }

    boolean dispatch(DelayContext ctx)
            throws IOException, InterruptedException, TimeoutException {
        var delay = options.policy().delay(ctx);
        if (delay.toMillis() <= 0) {
            var confirmed = publish(
                    options.topology().ready().exchange(),
                    ctx.control().routingKey(),
                    true,
                    ctx.toReadyProps(),
                    ctx.envelope().body());
            if (confirmed) {
                metric(ctx, true, Duration.ZERO);
            }
            return confirmed;
        }
        var tier = selectTier(delay);
        var confirmed = publish(
                "",
                options.topology().waitQueue().queue(),
                false,
                ctx.toHopProps(tier),
                ctx.envelope().body());
        if (confirmed) {
            metric(ctx, false, tier.duration());
        }
        return confirmed;
    }

    boolean reenter(DelayContext ctx)
            throws IOException, InterruptedException, TimeoutException {
        return publish(
                options.topology().rehop().exchange(),
                "",
                false,
                ctx.toHopProps(null),
                ctx.envelope().body());
    }

    private void metric(DelayContext ctx, boolean ready, Duration appliedDelay) {
        var control = ctx.control();
        var metrics = options.metrics();
        if (ready) {
            metrics.onPublishReady(control.id(), control.routingKey());
        } else {
            metrics.onRehop(control.id(), control.attempts(), appliedDelay);
        }
        if (log.isDebugEnabled()) {
            var now = options.clock().instant();
            var offset = Duration.between(control.deadline(), now.plus(appliedDelay));
            log.debug("[Delay] {} enqueued id={} rk={} attempts={} deadline={} offset={}",
                    ready ? "READY" : "WAIT",
                    control.id(), control.routingKey(), control.attempts(),
                    control.deadline(), offset);
        }
    }

    private boolean publish(
            String exchange,
            String routingKey,
            boolean mandatory,
            AMQP.BasicProperties props,
            byte[] body
    ) throws IOException, InterruptedException, TimeoutException {
        try (var conn = options.connectionFactory().createConnection();
             var channel = conn.createChannel(false)) {
            channel.confirmSelect();
            channel.basicPublish(exchange, routingKey, mandatory, props, body);
            return channel.waitForConfirms(options.confirmTimeout().toMillis());
        }
    }
}
