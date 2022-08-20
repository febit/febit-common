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
package org.febit.form.util;

import org.febit.bean.*;
import org.febit.form.BaseFormImpl;
import org.febit.form.meta.AM;
import org.febit.form.meta.Add;
import org.febit.form.meta.FormProfile;
import org.febit.form.meta.Modify;
import org.febit.lang.ClassMap;
import org.febit.util.ArraysUtils;
import org.febit.util.ClassUtil;
import org.febit.util.Maps;
import org.febit.vtor.BaseVtorChecker;
import org.febit.vtor.BaseVtorChecker.CheckConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

/**
 *
 * @author zqq90
 */
public class BaseFormUtil {

    private static final Logger LOG = LoggerFactory.getLogger(BaseFormUtil.class);

    private static final Peer[] EMPTY_PEERS = new Peer[0];
    private static final ClassMap<FormEntry> FORM_ENTRY_CACHING = new ClassMap<>(128);
    private static final ClassMap<Class> MODEL_TYPE_CACHING = new ClassMap<>(128);
    private static final ClassMap<Integer> PROFILE_CACHING = new ClassMap<>();

    static final BaseVtorChecker VTOR_CHECKER = new BaseVtorChecker();

    protected static class FormEntry {

        protected final Map<Integer, Peer[]> addProfiles;
        protected final Map<Integer, Peer[]> modifyProfiles;

        public FormEntry(Map<Integer, Peer[]> addProfiles, Map<Integer, Peer[]> modifyProfiles) {
            this.addProfiles = addProfiles;
            this.modifyProfiles = modifyProfiles;
        }
    }

    protected static class FormField {

        final int[] addProfiles;
        final int[] modifyProfiles;
        final FieldInfo fieldInfo;

        public FormField(int[] addProfiles, int[] modifyProfiles, FieldInfo fieldInfo) {
            this.addProfiles = addProfiles;
            this.modifyProfiles = modifyProfiles;
            this.fieldInfo = fieldInfo;
        }
    }

    protected static class Peer {

        final String name;
        final Getter from;
        final Setter to;
        final CheckConfig[] checkConfigs;

        protected Peer(String name, Getter from, Setter to, CheckConfig[] checkConfigs) {
            this.name = name;
            this.from = from;
            this.to = to;
            this.checkConfigs = checkConfigs;
        }

        protected void transfer(Object fromBean, Object toBean) {
            this.to.set(toBean, this.from.get(fromBean));
        }
    }

    public static int getFormProfile(Class<?> actionClass) {
        final Integer profile = PROFILE_CACHING.unsafeGet(actionClass);
        if (profile != null) {
            return profile;
        }
        final FormProfile profileAnno = actionClass.getAnnotation(FormProfile.class);
        return PROFILE_CACHING.putIfAbsent(actionClass, profileAnno != null
                ? profileAnno.value() : FormProfile.DEFAULT);
    }

    public static Class<?> getModelType(final Class<? extends BaseFormImpl> formClass) {
        Class<?> type = MODEL_TYPE_CACHING.unsafeGet(formClass);
        if (type != null) {
            return type;
        }
        type = BaseFormUtil.resolveModelType(formClass);
        MODEL_TYPE_CACHING.putIfAbsent(formClass, type);
        return type;
    }

    private static Class<?> resolveModelType(final Class<? extends BaseFormImpl> formClass) {
        return jodd.util.ClassUtil.getRawType(BaseFormImpl.class.getTypeParameters()[0], formClass);
    }

    static Peer[] getPeers(BaseFormImpl from, boolean add, int profile) {
        FormEntry formEntry = getFormEntry(from.getClass());
        Peer[] peers = (Peer[]) (add ? formEntry.addProfiles : formEntry.modifyProfiles).get(profile);
        if (peers == null) {
            LOG.debug("Peers not found for: from={}, add={}, profile={}", from, add, profile);
            return EMPTY_PEERS;
        }
        return peers;
    }

    public static void valid(BaseFormImpl from, boolean add, int profile) {
        for (Peer peer : getPeers(from, add, profile)) {
            VTOR_CHECKER.check(from, peer.checkConfigs, from::addVtor);
        }
    }

    public static void transfer(BaseFormImpl from, Object dest, boolean add, int profile) {
        Objects.requireNonNull(add, "dest is required");
        for (Peer peer : getPeers(from, add, profile)) {
            peer.transfer(from, dest);
        }
    }

    public static Map<String, Object> modifyMap(BaseFormImpl form, int profile) {
        Peer[] peers = getPeers(form, false, profile);
        if (peers.length == 0) {
            return Collections.emptyMap();
        }
        Map<String, Object> ret = Maps.create(profile);
        for (Peer peer : peers) {
            ret.put(peer.name, peer.from.get(form));
        }
        return ret;
    }

    private static FormEntry getFormEntry(final Class<? extends BaseFormImpl> formClass) {
        FormEntry formEntry = FORM_ENTRY_CACHING.unsafeGet(formClass);
        if (formEntry != null) {
            return formEntry;
        }
        return FORM_ENTRY_CACHING.putIfAbsent(formClass, resolveFormEntry(formClass));
    }

    private static FormEntry resolveFormEntry(final Class<? extends BaseFormImpl> formClass) {
        return resolveFormEntry(formClass, getModelType(formClass));
    }

    private static FormEntry resolveFormEntry(final Class<?> formClass, final Class<?> receiverClass) {
        final Map<String, FieldInfo> receiverFieldInfoMap = new HashMap<>();
        FieldInfoResolver.of(receiverClass)
                .forEach(receiverFieldInfoMap::put);

        final Map<Integer, Set<Peer>> adds = new HashMap<>(16);
        final Map<Integer, Set<Peer>> modifys = new HashMap<>(16);
        final List<CheckConfig> checkConfigsBuf = new ArrayList<>();
        scanFormField(formClass, formItem -> {
            final String name = formItem.fieldInfo.name;
            final FieldInfo toFieldInfo = receiverFieldInfoMap.get(name);
            if (toFieldInfo == null) {
                throw new RuntimeException("Not found property '" + name + "' in class '" + receiverClass.getName() + "'");
            }

            // resolve check configs:
            // clear and reuse list
            checkConfigsBuf.clear();
            VTOR_CHECKER.collectCheckConfig(formItem.fieldInfo, checkConfigsBuf::add);
            CheckConfig[] checkConfigs = checkConfigsBuf.isEmpty()
                    ? BaseVtorChecker.emptyCheckConfigs()
                    : checkConfigsBuf.toArray(new CheckConfig[checkConfigsBuf.size()]);

            // create peer
            final Peer peer = new Peer(
                    name,
                    AccessFactory.createGetter(formItem.fieldInfo),
                    AccessFactory.createSetter(toFieldInfo),
                    checkConfigs
            );

            if (formItem.addProfiles != null) {
                for (int i : formItem.addProfiles) {
                    adds.computeIfAbsent(i, key -> new HashSet<>())
                            .add(peer);
                }
            }

            if (formItem.modifyProfiles != null) {
                for (int i : formItem.modifyProfiles) {
                    modifys.computeIfAbsent(i, key -> new HashSet<>())
                            .add(peer);
                }
            }
        });
        //collect
        final Map<Integer, Peer[]> addProfiles = Maps.create(adds.size());
        adds.forEach((k, peers) -> addProfiles.put(k, peersToArray(peers)));
        final Map<Integer, Peer[]> modifyProfiles = Maps.create(modifys.size());
        modifys.forEach((k, peers) -> modifyProfiles.put(k, peersToArray(peers)));
        return new FormEntry(addProfiles, modifyProfiles);
    }

    private static boolean notEmpty(int[] arr) {
        return arr != null && arr.length != 0;
    }

    private static Peer[] peersToArray(Collection<Peer> peers) {
        if (peers.isEmpty()) {
            return EMPTY_PEERS;
        }
        return peers.toArray(new Peer[peers.size()]);
    }

    /**
     *
     * @param fieldInfo
     * @return null if not match
     */
    private static FormField toFormField(FieldInfo fieldInfo) {
        Field field = fieldInfo.getField();
        if (field == null) {
            return null;
        }
        Add a = field.getAnnotation(Add.class);
        Modify m = field.getAnnotation(Modify.class);
        AM am = field.getAnnotation(AM.class);
        int[] addProfiles = null;
        int[] modifyProfiles = null;
        if (a != null) {
            addProfiles = a.value();
        }
        if (m != null) {
            modifyProfiles = m.value();
        }
        if (am != null && notEmpty(am.value())) {
            final int[] amProfiles = am.value();
            addProfiles = notEmpty(addProfiles)
                    ? ArraysUtils.join(addProfiles, amProfiles)
                    : amProfiles;

            modifyProfiles = notEmpty(modifyProfiles)
                    ? ArraysUtils.join(modifyProfiles, amProfiles)
                    : amProfiles;
        }
        if (!notEmpty(addProfiles)
                && !notEmpty(modifyProfiles)) {
            LOG.debug("Skip field: {}", field);
            return null;
        }
        return new FormField(addProfiles, modifyProfiles, fieldInfo);
    }

    private static void scanFormField(final Class<?> formClass, Consumer<FormField> consumer) {
        FieldInfoResolver.of(formClass)
                .overrideFieldFilter(f -> ClassUtil.notStatic(f) && f.getAnnotations().length != 0)
                .stream()
                .filter(f -> f.getField() != null && f.isGettable())
                .map(BaseFormUtil::toFormField)
                .filter(Objects::nonNull)
                .forEach(consumer);
    }

}
