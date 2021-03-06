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
public class BetweenCheck extends AbstractLongCheck<Between> {

    @Override
    protected Object[] check(Between anno, double value) {
        if (value < (double) anno.min()
                || value > (double) anno.max()) {
            return new Object[]{anno.min(), anno.max(), value};
        }
        return null;
    }

    @Override
    protected Object[] check(Between anno, long value) {
        if (value < anno.min()
                || value > anno.max()) {
            return new Object[]{anno.min(), anno.max(), value};
        }
        return null;
    }

    @Override
    public String getDefaultMessage(Object[] result) {
        return "Please pass a number between {0} and {1}";
    }
}
