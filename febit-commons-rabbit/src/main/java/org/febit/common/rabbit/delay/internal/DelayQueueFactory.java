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

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import org.febit.common.rabbit.delay.DelayQueueOptions;
import org.febit.common.rabbit.delay.RabbitDelayQueue;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@UtilityClass
public class DelayQueueFactory {

    public static RabbitDelayQueue create(DelayQueueOptions options) {
        var topologyReady = declareTopology(options);
        var container = createContainer(options);
        container.start();
        var topology = options.topology();
        if (topologyReady) {
            log.info("[Delay] queue started name={} wait={} rehop={} ready={}",
                    options.name(), topology.waitQueue().queue(), topology.rehop().queue(),
                    topology.ready().exchange());
        } else {
            // started anyway: the container recovers on its own once the topology is fixed
            log.warn("[Delay] queue started with incomplete topology name={} wait={} rehop={} ready={}",
                    options.name(), topology.waitQueue().queue(), topology.rehop().queue(),
                    topology.ready().exchange());
        }
        return DelayQueueImpl.create(options, container);
    }

    private static SimpleMessageListenerContainer createContainer(DelayQueueOptions options) {
        var container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(options.connectionFactory());
        container.setQueueNames(options.topology().rehop().queue());
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setConcurrentConsumers(options.consumer().concurrency());
        container.setPrefetchCount(options.consumer().prefetch());
        if (options.consumer().virtualThread()) {
            var executor = new SimpleAsyncTaskExecutor();
            executor.setVirtualThreads(true);
            container.setTaskExecutor(executor);
        }
        container.setMessageListener(new RehopListener(options));
        return container;
    }

    /**
     * @return {@code true} when every owned resource was declared
     */
    private static boolean declareTopology(DelayQueueOptions options) {
        var cf = options.connectionFactory();
        var topology = options.topology();

        var admin = new RabbitAdmin(cf);
        var rehopEx = new FanoutExchange(topology.rehop().exchange(), true, false);
        var rehopDlxEx = new FanoutExchange(topology.dlx().exchange(), true, false);
        var readyEx = new TopicExchange(topology.ready().exchange(), true, false);

        var failed = new ArrayList<String>();

        declareOwned(admin, rehopEx, failed);
        declareOwned(admin, rehopDlxEx, failed);
        // supplied by the application: may be pre-declared, or not configurable by us
        declareExternal(admin, readyEx);

        var rehopQ = QueueBuilder.durable(topology.rehop().queue())
                .deadLetterExchange(topology.dlx().exchange())
                .deadLetterRoutingKey(topology.dlx().queue())
                .build();
        declareOwned(admin, rehopQ, failed);
        declareOwned(admin, BindingBuilder.bind(rehopQ).to(rehopEx), failed);

        var rehopDlxQ = QueueBuilder.durable(topology.dlx().queue()).build();
        declareOwned(admin, rehopDlxQ, failed);
        declareOwned(admin, BindingBuilder.bind(rehopDlxQ).to(rehopDlxEx), failed);

        var waitQ = QueueBuilder.durable(topology.waitQueue().queue())
                .quorum()
                .deadLetterExchange(topology.rehop().exchange())
                .deadLetterRoutingKey("")
                .build();
        declareOwned(admin, waitQ, failed);

        if (!failed.isEmpty()) {
            // Could not ensure these exist: they may be missing, or may already exist with
            // different arguments (e.g. classic vs quorum) — both surface the same way.
            log.error("[Delay] topology declaration failed name={} failed={}, deliveries may fail",
                    options.name(), failed);
            return false;
        }
        log.info("[Delay] topology declared wait={} rehop={} dlx={} ready={}",
                topology.waitQueue().queue(), topology.rehop().exchange(), topology.dlx().exchange(),
                topology.ready().exchange());
        return true;
    }

    private static void declareOwned(RabbitAdmin admin, Declarable d, List<String> failed) {
        try {
            declare(admin, d);
        } catch (RuntimeException e) {
            var name = nameOf(d);
            failed.add(name);
            log.warn("[Delay] declare failed d={}", name, e);
        }
    }

    /**
     * Best effort: the ready exchange belongs to the application.  It may already exist with
     * different arguments, or the app may hold no configure permission — neither is fatal.
     */
    private static void declareExternal(RabbitAdmin admin, Declarable d) {
        try {
            declare(admin, d);
        } catch (RuntimeException e) {
            log.debug("[Delay] declare skipped for externally owned resource d={}", nameOf(d), e);
        }
    }

    private static void declare(RabbitAdmin admin, Declarable d) {
        switch (d) {
            case Exchange ex -> admin.declareExchange(ex);
            case Queue q -> admin.declareQueue(q);
            case Binding b -> admin.declareBinding(b);
            default -> log.warn("[Delay] skip unsupported declarable d={}", d);
        }
    }

    private static String nameOf(Declarable d) {
        return switch (d) {
            case Exchange ex -> ex.getName();
            case Queue q -> q.getName();
            case Binding b -> b.getDestination() + "->" + b.getExchange();
            default -> String.valueOf(d);
        };
    }
}
