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

import org.febit.form.util.BaseFormUtil;
import org.febit.util.ClassUtil;
import org.febit.vtor.Vtor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author zqq90
 * @param <E>
 * @param <I>
 */
public abstract class BaseFormImpl<E, I> implements AddForm<E>, ModifyForm<E, I> {

    protected List<Vtor> __vtors;

    @Override
    public E createAdded(final int profile) {
        final E model = ClassUtil.newInstance(modelType());
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
    public final boolean valid(int profile, boolean add) {
        BaseFormUtil.valid(this, add, profile);
        customValid(profile, add);
        return !hasVtor();
    }

    public abstract void customValid(int profile, boolean add);

    public boolean hasVtor() {
        List<Vtor> vtors = this.__vtors;
        return vtors != null && !vtors.isEmpty();
    }

    public void addVtor(Vtor vtor) {
        List<Vtor> vtors = this.__vtors;
        if (vtors == null) {
            vtors = new ArrayList<>();
            this.__vtors = vtors;
        }
        vtors.add(vtor);
    }

    @Override
    public List<Vtor> getVtors() {
        return __vtors;
    }

    @SuppressWarnings("unchecked")
    public Class<E> modelType() {
        return (Class<E>) BaseFormUtil.getModelType(this.getClass());
    }
}
