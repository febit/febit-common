/**
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

import java.util.ArrayList;
import java.util.List;
import jodd.util.ClassLoaderUtil;
import org.febit.lang.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.febit.util.ClassUtil;
import org.febit.util.Petite;
import org.febit.util.PriorityUtil;
import org.febit.util.Props;
import org.febit.util.PropsUtil;
import org.febit.util.Stopwatch;

/**
 *
 * @author zqq90
 */
public class App implements Singleton {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    protected Props _props = new Props();
    protected Listener[] _listeners;
    protected Petite _petite;

    //settings
    protected String name = "App";
    protected Object[] beans;

    public void start(String propsFiles) {
        Stopwatch stopwatch = Stopwatch.startNew();
        loadProps(propsFiles);
        initPetite();
        try {
            startListeners();
            stopwatch.stop();
            LOG.info("> App [{}] start in {} ms.", name, stopwatch.nowInMillis());
        } catch (Exception ex) {
            LOG.error("> Failed start: " + name, ex);
            stop();
            throw new RuntimeException(ex);
        }
    }

    public void stop() {
        Stopwatch stopwatch = Stopwatch.startNew();
        stopListeners();
        stopwatch.stop();
        LOG.info("> App [{}] stopped in {} ms.", name, stopwatch.nowInMillis());
    }

    protected void loadProps(String propsFiles) {
        if (propsFiles == null) {
            return;
        }
        if (propsFiles.indexOf('*') >= 0) {
            PropsUtil.scanClasspath(_props, propsFiles);
        } else {
            PropsUtil.load(_props, propsFiles);
        }
    }

    protected void initPetite() {
        LOG.info("Loaded props: {}", _props.getModulesString());
        this._petite = Petite.builder()
                .addProps(_props)
                .addGlobalBean(this)
                .buildWithServices();
        this._petite.register("app", this);
    }

    protected void startListeners() {
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
        this._listeners = listeners.toArray(new Listener[listeners.size()]);
        PriorityUtil.desc(this._listeners);
        for (Listener listener : this._listeners) {
            LOG.info(">> starting listener:" + listener.getClass());
            listener.start();
        }
    }

    protected void stopListeners() {
        Listener[] listeners = this._listeners;
        if (listeners == null) {
            return;
        }
        for (int i = listeners.length - 1; i >= 0; i--) {
            Listener listener = listeners[i];
            LOG.info(">> stoping listener:" + listener.getClass());
            listener.stop();
        }
        this._listeners = null;
    }

    public Object createBean(String type) throws ClassNotFoundException {
        return createBean(ClassLoaderUtil.getDefaultClassLoader().loadClass(type));
    }

    public Object createBean(Class type) {
        final Object bean = ClassUtil.newInstance(type);
        this._petite.inject(bean);
        return bean;
    }

    public Petite getPetite() {
        return this._petite;
    }

    public void injectBean(final Object bean) {
        this._petite.inject(bean);
    }

    public void addBean(final Object bean) {
        this._petite.register(bean);
    }

    public Object getBean(String name) {
        return this._petite.get(name);
    }

    public <T> T getBean(Class<T> type) {
        return (T) this._petite.get(type);
    }
}
