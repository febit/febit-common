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
package org.febit.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jodd.util.ReflectUtil;
import org.febit.form.util.BaseFormUtil;
import org.febit.lang.ConcurrentIdentityMap;
import org.febit.util.ClassUtil;
import org.febit.vtor.Vtor;

/**
 *
 * @author zqq90
 * @param <E>
 * @param <I>
 */
public abstract class BaseFormImpl<E, I> implements AddForm<E>, ModifyForm<E, I> {

    private static final ConcurrentIdentityMap<Class, Class> CACHE = new ConcurrentIdentityMap<>(128);
    protected List<Vtor> __vtors;

    @Override
    public E createAdded(final int profile) {
        final E model = (E) ClassUtil.newInstance(modelType());
        BaseFormUtil.transfer(this, model, true, profile);
        return model;
    }

    @Override
    public E updateModify(final E model, final int profile) {
        if (model == null) {
            throw new IllegalArgumentException("Model is required");
        }
        BaseFormUtil.transfer(this, model, false, profile);
        return model;
    }

    @Override
    public Map<String, Object> modifyMap(final int profile) {
        return BaseFormUtil.modifyMap(this, profile);
    }

    @Override
    public boolean valid(int profile, boolean add) {
        //FIXME: form valid() 
        customValid(profile, add);
        return true;
    }

    public abstract void customValid(int profile, boolean add);

    protected void addVtor(Vtor vtor) {
        if (this.__vtors == null) {
            this.__vtors = new ArrayList<>();
        }
        this.__vtors.add(vtor);
    }

    @Override
    public List<Vtor> getVtors() {
        return __vtors;
    }

    @SuppressWarnings("unchecked")
    public Class<E> modelType() {
        Class<E> type = CACHE.unsafeGet(this.getClass());
        if (type != null) {
            return type;
        }
        type = (Class<E>) ReflectUtil.getRawType(BaseFormImpl.class.getTypeParameters()[0], this.getClass());
        CACHE.putIfAbsent(this.getClass(), type);
        return type;
    }
}
