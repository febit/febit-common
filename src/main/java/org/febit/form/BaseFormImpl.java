package org.febit.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jodd.util.ReflectUtil;
import org.febit.form.util.BaseFormUtil;
import org.febit.lang.ConcurrentIdentityMap;
import org.febit.vtor.Vtor;

/**
 *
 * @author zqq90
 * @param <E>
 * @param <I>
 */
public abstract class BaseFormImpl<E, I> implements AddForm<E>, ModifyForm<E, I> {

    private static final ConcurrentIdentityMap<Class> CACHE = new ConcurrentIdentityMap<>(128);
    protected List<Vtor> __vtors;

    @Override
    public E createAdded(final int profile) {
        final E model;
        try {
            model = (E) modelType().newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
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

    public Class modelType() {
        Class type = CACHE.unsafeGet(this.getClass());
        if (type != null) {
            return type;
        }
        type = ReflectUtil.getRawType(BaseFormImpl.class.getTypeParameters()[0], this.getClass());
        CACHE.putIfAbsent(this.getClass(), type);
        return type;
    }
}
