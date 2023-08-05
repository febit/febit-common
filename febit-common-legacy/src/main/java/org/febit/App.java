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
package org.febit;

import jodd.util.ClassLoaderUtil;
import org.apache.commons.lang3.time.StopWatch;
import org.febit.lang.Singleton;
import org.febit.util.ClassUtil;
import org.febit.util.Petite;
import org.febit.util.Priority;
import org.febit.util.Props;
import org.febit.util.PropsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zqq90
 */
public class App implements Singleton {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    private final Props props = new Props();
    private Listener[] startedListeners;
    private Petite petite;

    //settings
    protected String name = "App";
    protected Object[] beans;

    public synchronized void start(String propsFiles) {
        var stopwatch = StopWatch.createStarted();
        this.props.clear();
        loadProps(propsFiles);
        initPetite();
        try {
            startListeners();
            LOG.info("> App [{}] start in {} ms.", name, stopwatch.getTime());
        } catch (Exception ex) {
            LOG.error("> Failed start: " + name, ex);
            stop();
            throw new RuntimeException(ex);
        }
    }

    public synchronized void stop() {
        var stopwatch = StopWatch.createStarted();
        stopListeners();
        LOG.info("> App [{}] stopped in {} ms.", name, stopwatch.getTime());
    }

    protected void loadProps(String propsFiles) {
        if (propsFiles == null) {
            return;
        }
        if (propsFiles.indexOf('*') >= 0) {
            PropsUtil.scanClasspath(props, propsFiles);
        } else {
            PropsUtil.load(props, propsFiles);
        }
    }

    protected void initPetite() {
        LOG.info("Loaded props: {}", props.getModulesString());
        this.petite = Petite.builder()
                .addProps(props)
                .addGlobalBean(this)
                .buildWithServices();
        this.petite.register("app", this);
    }

    protected synchronized void startListeners() {
        stopListeners();
        if (this.beans == null) {
            return;
        }
        final List<Listener> listeners = new ArrayList<>(this.beans.length);
        for (Object bean : this.beans) {
            if (bean instanceof Listener) {
                listeners.add((Listener) bean);
            }
        }
        if (listeners.isEmpty()) {
            return;
        }
        this.startedListeners = listeners.toArray(new Listener[listeners.size()]);
        Priority.desc(this.startedListeners);
        for (Listener listener : this.startedListeners) {
            LOG.info(">> starting listener:" + listener.getClass());
            listener.start();
        }
    }

    protected synchronized void stopListeners() {
        Listener[] listeners = this.startedListeners;
        if (listeners == null) {
            return;
        }
        for (int i = listeners.length - 1; i >= 0; i--) {
            Listener listener = listeners[i];
            LOG.info(">> stoping listener:" + listener.getClass());
            listener.stop();
        }
        this.startedListeners = null;
    }

    public Object createBean(String type) throws ClassNotFoundException {
        return createBean(ClassLoaderUtil.getDefaultClassLoader().loadClass(type));
    }

    public <T> T createBean(Class<T> type) {
        final T bean = ClassUtil.newInstance(type);
        this.petite.inject(bean);
        return bean;
    }

    public Props getProps() {
        return props;
    }

    public Petite getPetite() {
        return this.petite;
    }

    public void injectBean(final Object bean) {
        this.petite.inject(bean);
    }

    public void addBean(final Object bean) {
        this.petite.register(bean);
    }

    public Object getBean(String name) {
        return this.petite.get(name);
    }

    public <T> T getBean(Class<T> type) {
        return this.petite.get(type);
    }
}
