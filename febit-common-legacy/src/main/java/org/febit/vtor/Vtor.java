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
package org.febit.vtor;

import org.febit.util.StringUtil;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * A simple validate util, idea from jodd-vtor.
 *
 * @author zqq90
 */
@Deprecated
public class Vtor {

    private static class Holder {

        static final VtorChecker CHECKER = new VtorChecker();
    }

    /**
     * Check bean.
     *
     * @param bean bean to check
     * @return an empty array will returned if all passed.
     */
    public static Vtor[] check(Object bean) {
        return Holder.CHECKER.check(bean);
    }

    /**
     * Check bean.
     *
     * @param bean   bean to check
     * @param filter CheckConfig filter, please return true if accept/allow the Check
     * @return an empty array will returned if all passed.
     */
    public Vtor[] check(Object bean, Predicate<VtorChecker.CheckConfig> filter) {
        return Holder.CHECKER.check(bean, filter);
    }

    public static Vtor create(String name, Check check, Object[] args) {
        return new Vtor(name, check.getDefaultMessage(args), check, args);
    }

    public static Vtor create(String name, String message, Object[] args) {
        return new Vtor(name, message, null, args);
    }

    public static Vtor create(String name, String message, Check check, Object[] args) {
        return new Vtor(name, message, check, args);
    }

    public final String name;
    public final String message;
    public final Check check;
    public final Object[] args;

    public Vtor(String name, String message, Check check, Object[] args) {
        this.name = name;
        this.message = message;
        this.check = check;
        this.args = args;
    }

    public String formatMessage() {
        return StringUtil.format(this.message, args);
    }

    public String formatMessage(String template) {
        return StringUtil.format(template, args);
    }

    @Override
    public String toString() {
        return "Vtor{"
                + "name=" + name
                + ", message=" + message
                + ", check=" + check
                + ", args="
                + Arrays.toString(args)
                + '}';
    }

}
