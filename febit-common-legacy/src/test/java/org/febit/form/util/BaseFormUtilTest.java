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

import org.febit.form.BaseFormImpl;
import org.febit.form.meta.AM;
import org.febit.form.meta.Add;
import org.febit.form.meta.Modify;
import org.febit.form.util.BaseFormUtil.Peer;
import org.febit.vtor.Check;
import org.febit.vtor.Length;
import org.febit.vtor.Min;
import org.febit.vtor.NotEmpty;
import org.febit.vtor.NotNull;
import org.febit.vtor.Numeric;
import org.febit.vtor.Vtor;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author zqq90
 */
public class BaseFormUtilTest {

    public static class Foo {

        protected long id;

        public String publicString;
        public int publicInt;

        protected String protectedString;

        private Object privateObject;

        // without getter/setter but accessable
        private Object privateObject2;

        // without getter/setter but accessable
        private Object privateObject3;

        public Object getPrivateObject() {
            return privateObject;
        }
    }

    public static class ParentForm extends BaseFormImpl<Foo, Long> {

        protected long id;

        @AM({1, 2, 101, 200})
        @Add({2, 3, 4})
        @Modify({4})
        @NotNull
        @NotEmpty
        @Length(min = 2, max = 10)
        public String publicString;

        @Add({2, 102, 200})
        @NotNull
        private Object privateObject;

        // will be excluded, unaccessable!
        @NotNull
        @AM({1, 2, 200})
        private Object privateObject3;

        public Object getPrivateObject() {
            return privateObject;
        }

        @Override
        public void customValid(int profile, boolean add) {
        }

        @Override
        public Long id() {
            return id;
        }
    }

    public static class ChildForm extends ParentForm {

        public int publicInt;

        @Add({1, 2, 200})
        @Modify({1, 2})
        @AM({103})
        @Numeric
        protected String protectedString;

        // accessable
        @Modify({1})
        @AM({104, 200})
        @NotNull
        @Min(100)
        private Object privateObject2;

    }

    protected static Check getCheck(Class<? extends Annotation> annoType) {
        return BaseFormUtil.VTOR_CHECKER.getCheck(annoType);
    }

    @Test
    public void test_getModelType() {
        assertEquals(BaseFormUtil.getModelType(ChildForm.class), Foo.class);
        assertEquals(BaseFormUtil.getModelType(ParentForm.class), Foo.class);
    }

    @Test
    public void test_valid() {
        ChildForm childForm = new ChildForm();
        childForm.privateObject2 = 23;
        childForm.protectedString = "not number";
        childForm.publicString = "";

        Peer[] peers;
        peers = BaseFormUtil.getPeers(childForm, true, 200);
        assertEquals(peers.length, 4);

        BaseFormUtil.valid(childForm, true, 200);
        assertTrue(childForm.hasVtor());
        assertEquals(childForm.getVtors().size(), 5);
        for (Vtor vtor : childForm.getVtors()) {
            switch (vtor.name) {
                case "publicString":
                    assertTrue(
                            vtor.check == getCheck(NotEmpty.class)
                                    || vtor.check == getCheck(Length.class)
                    );
                    break;
                case "protectedString":
                    assertSame(vtor.check, getCheck(Numeric.class));
                    break;
                case "privateObject2":
                    assertSame(vtor.check, getCheck(Min.class));
                    break;
                case "privateObject":
                    assertSame(vtor.check, getCheck(NotNull.class));
                    break;
                default:
                    fail("unreachable: " + vtor.name);
            }
        }

    }

    @Test
    public void test_getPeers() {

        Peer[] peers;

        ChildForm childForm = new ChildForm();

        peers = BaseFormUtil.getPeers(childForm, true, 0);
        assertEquals(peers.length, 0);
        peers = BaseFormUtil.getPeers(childForm, false, 0);
        assertEquals(peers.length, 0);

        peers = BaseFormUtil.getPeers(childForm, true, -1);
        assertEquals(peers.length, 0);
        peers = BaseFormUtil.getPeers(childForm, false, -1);
        assertEquals(peers.length, 0);

        peers = BaseFormUtil.getPeers(childForm, true, 1);
        assertEquals(peers.length, 2);

        peers = BaseFormUtil.getPeers(childForm, true, 2);
        assertEquals(peers.length, 3);

        peers = BaseFormUtil.getPeers(childForm, true, 3);
        assertEquals(peers.length, 1);

        peers = BaseFormUtil.getPeers(childForm, false, 1);
        assertEquals(peers.length, 3);

        peers = BaseFormUtil.getPeers(childForm, false, 2);
        assertEquals(peers.length, 2);

        peers = BaseFormUtil.getPeers(childForm, false, 4);
        assertEquals(peers.length, 1);

        peers = BaseFormUtil.getPeers(childForm, true, 101);
        assertEquals(peers.length, 1);
        assertEquals(peers[0].checkConfigs.length, 3);

        peers = BaseFormUtil.getPeers(childForm, true, 102);
        assertEquals(peers.length, 1);
        assertEquals(peers[0].checkConfigs.length, 1);
        peers = BaseFormUtil.getPeers(childForm, true, 103);
        assertEquals(peers.length, 1);
        assertEquals(peers[0].checkConfigs.length, 1);
        peers = BaseFormUtil.getPeers(childForm, true, 104);
        assertEquals(peers.length, 1);
        assertEquals(peers[0].checkConfigs.length, 2);

    }
}
