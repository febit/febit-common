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
package org.febit.form.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jodd.util.ReflectUtil;
import jodd.util.collection.IntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.febit.bean.AccessFactory;
import org.febit.bean.FieldInfo;
import org.febit.bean.FieldInfoResolver;
import org.febit.bean.Getter;
import org.febit.bean.Setter;
import org.febit.form.BaseFormImpl;
import org.febit.form.meta.AM;
import org.febit.form.meta.Add;
import org.febit.form.meta.Modify;
import org.febit.lang.ConcurrentIdentityMap;
import org.febit.util.ArraysUtil;
import org.febit.util.ClassUtil;
import org.febit.util.CollectionUtil;

/**
 *
 * @author zqq90
 */
public class BaseFormUtil {

    private static final Logger LOG = LoggerFactory.getLogger(BaseFormUtil.class);
    private static final ConcurrentIdentityMap<FormEntry> CACHE = new ConcurrentIdentityMap<>(128);

    protected static class FormEntry {

        protected final IntHashMap addProfiles;
        protected final IntHashMap modifyProfiles;

        public FormEntry(IntHashMap addProfiles, IntHashMap modifyProfiles) {
            this.addProfiles = addProfiles;
            this.modifyProfiles = modifyProfiles;
        }
    }

    public static void transfer(BaseFormImpl from, Object dest, boolean add, int profile) {
        FormEntry formEntry = getFormEntry(from.getClass());
        if (dest == null) {
            throw new IllegalArgumentException("dest is required");
        }
        Peer[] peers = (Peer[]) (add ? formEntry.addProfiles : formEntry.modifyProfiles).get(profile);
        if (peers != null) {
            for (int i = 0, len = peers.length; i < len; i++) {
                peers[i].transfer(from, dest);
            }
        } else {
            LOG.info("transfer nothing: from='{}', dest='{}', add='{}', profile='{}'", from, dest, add, profile);
        }
    }

    public static Map<String, Object> modifyMap(BaseFormImpl from, int profile) {
        FormEntry formEntry = getFormEntry(from.getClass());
        Peer[] peers = (Peer[]) formEntry.modifyProfiles.get(profile);
        if (peers != null) {
            Map<String, Object> ret = CollectionUtil.createHashMap(profile);
            for (int i = 0, len = peers.length; i < len; i++) {
                Peer peer = peers[i];
                ret.put(peer.name, peer.from.get(from));
            }
            return ret;
        } else {
            LOG.info("transfer nothing: from='{}' profile='{}'", from, profile);
            return Collections.EMPTY_MAP;
        }
    }

    protected static class Peer {

        final Setter to;
        final Getter from;
        final String name;

        protected Peer(Setter to, Getter from, String name) {
            this.to = to;
            this.from = from;
            this.name = name;
        }

        protected void transfer(Object fromBean, Object toBean) {
            this.to.set(toBean, this.from.get(fromBean));
        }
    }

    protected static FormEntry getFormEntry(final Class providerType) {
        FormEntry formEntry = CACHE.unsafeGet(providerType);
        if (formEntry != null) {
            return formEntry;
        } else {
            return resolveFormEntry(providerType);
        }
    }

    protected static FormEntry resolveFormEntry(final Class providerType) {
        final Class receiverType = ReflectUtil.getRawType(BaseFormImpl.class.getTypeParameters()[0], providerType);
        final List<FormItem> formItems = new FormItemResolver(providerType).resolve();
        final Map<Integer, List<Peer>> adds = new HashMap<>(16);
        final Map<Integer, List<Peer>> modifys = new HashMap<>(16);
        final Map<String, FieldInfo> receiverFieldInfoMap;
        {
            final FieldInfo[] fieldInfos = FieldInfoResolver.resolve(receiverType);
            receiverFieldInfoMap = new HashMap(fieldInfos.length * 4 / 3 + 1);
            for (FieldInfo fieldInfo : fieldInfos) {
                receiverFieldInfoMap.put(fieldInfo.name, fieldInfo);
            }
        }

        for (FormItem formItem : formItems) {
            Field field = formItem.field;
            FieldInfo fieldInfo = receiverFieldInfoMap.get(field.getName());
            if (fieldInfo == null) {
                throw new RuntimeException("Not Found property'" + field.getName() + "' in class '" + receiverType.getName() + "'");
            }
            Setter setter = AccessFactory.createSetter(fieldInfo);
            Getter getter;
            Method getterMethod = ClassUtil.getPublicGetterMethod(field, providerType);
            if (getterMethod == null) {
                //LOG.warn("Used FieldGetter:" + field);
                getter = AccessFactory.createGetter(field);
            } else {
                getter = AccessFactory.createGetter(getterMethod);
            }
            final Peer peer = new Peer(setter, getter, fieldInfo.name);
            //
            int[] addProfiles = formItem.addProfiles;
            if (addProfiles != null) {
                for (int i : addProfiles) {
                    List<Peer> peers = (List<Peer>) adds.get(i);
                    if (peers == null) {
                        peers = new ArrayList<>();
                        adds.put(i, peers);
                    }
                    peers.add(peer);
                }
            }
            //
            int[] modifyProfiles = formItem.modifyProfiles;
            if (modifyProfiles != null) {
                for (int i : modifyProfiles) {
                    List<Peer> peers = (List<Peer>) modifys.get(i);
                    if (peers == null) {
                        peers = new ArrayList<>();
                        modifys.put(i, peers);
                    }
                    peers.add(peer);
                }
            }
        }
        //collect
        final IntHashMap addProfiles = CollectionUtil.createIntHashMap(adds.size());;
        for (Map.Entry<Integer, List<Peer>> entry : adds.entrySet()) {
            Integer integer = entry.getKey();
            List<Peer> list = entry.getValue();
            addProfiles.put(integer, list.toArray(new Peer[list.size()]));
        }
        final IntHashMap modifyProfiles = CollectionUtil.createIntHashMap(modifys.size());
        for (Map.Entry<Integer, List<Peer>> entry : modifys.entrySet()) {
            Integer integer = entry.getKey();
            List<Peer> list = entry.getValue();
            modifyProfiles.put(integer, list.toArray(new Peer[list.size()]));
        }
        return CACHE.putIfAbsent(providerType, new FormEntry(addProfiles, modifyProfiles));
    }

    protected static class FormItem {

        final int[] addProfiles;
        final int[] modifyProfiles;
        final Field field;

        public FormItem(int[] addProfiles, int[] modifyProfiles, Field field) {
            this.addProfiles = addProfiles;
            this.modifyProfiles = modifyProfiles;
            this.field = field;
        }
    }

    /**
     * XXX: 没有剔除子类中覆盖父类的字段
     */
    protected static class FormItemResolver {

        private final Class beanType;
        private final List<FormItem> result;

        protected FormItemResolver(Class beanType) {
            this.beanType = beanType;
            this.result = new ArrayList<>();
        }

        protected List<FormItem> resolve() {
            resolve(beanType);
            return result;
        }

        private void resolve(final Class type) {
            if (type == null || type.equals(Object.class)) {
                return;
            }
            final Field[] fields = type.getDeclaredFields();
            for (int i = 0, len = fields.length; i < len; i++) {
                final Field field = fields[i];
                int[] addProfiles = null;
                int[] modifyProfiles = null;
                Add a = field.getAnnotation(Add.class);
                Modify m = field.getAnnotation(Modify.class);
                AM am = field.getAnnotation(AM.class);
                if (a != null) {
                    addProfiles = a.value();
                }
                if (m != null) {
                    modifyProfiles = m.value();
                }
                if (am != null) {
                    final int[] amProfiles = am.value();
                    if (amProfiles != null && amProfiles.length != 0) {
                        if (addProfiles != null && addProfiles.length != 0) {
                            addProfiles = ArraysUtil.join(addProfiles, amProfiles);
                        } else {
                            addProfiles = amProfiles;
                        }
                        if (modifyProfiles != null && modifyProfiles.length != 0) {
                            modifyProfiles = ArraysUtil.join(modifyProfiles, amProfiles);
                        } else {
                            modifyProfiles = amProfiles;
                        }
                    }
                }
                if (addProfiles != null && addProfiles.length == 0) {
                    addProfiles = null;
                }
                if (modifyProfiles != null && modifyProfiles.length == 0) {
                    modifyProfiles = null;
                }
                if (addProfiles != null
                        || modifyProfiles != null) {
                    result.add(new FormItem(addProfiles, modifyProfiles, field));
                } else {
                    LOG.debug("Skip field: {}", field);
                }
            }
            resolve(type.getSuperclass());
        }
    }

}
