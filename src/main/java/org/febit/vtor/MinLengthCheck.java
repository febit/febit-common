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
package org.febit.vtor;

/**
 *
 * @author zqq90
 */
public class MinLengthCheck implements Check<MinLength> {

    @Override
    public Object[] check(MinLength anno, Object value) {
        if (value == null) {
            return null;
        }
        int len = value.toString().length();
        if (len < anno.value()) {
            return new Object[]{anno.value(), len};
        }
        return null;
    }

    @Override
    public String getDefaultMessage(Object[] result) {
        return "Please pass a string length is at least {0}";
    }
}
