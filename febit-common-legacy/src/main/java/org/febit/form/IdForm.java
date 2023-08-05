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
package org.febit.form;

import org.febit.form.util.FieldUtil;
import org.febit.util.StringUtil;

/**
 * @author zqq90
 */
public class IdForm {

    protected String id;

    public boolean requiredCheck() {
        return StringUtil.isNotEmpty(id);
    }

    public String getStringId() {
        if (StringUtil.isEmpty(id)) {
            return null;
        }
        return id;
    }

    public Short getShortId() {
        return FieldUtil.toShort(id);
    }

    public short getShortId(short defaultValue) {
        return FieldUtil.toShort(id, defaultValue);
    }

    public int getIntId() {
        return FieldUtil.toInt(id);
    }

    public int getIntegerId() {
        return FieldUtil.toInt(id);
    }

    public int getIntId(int defaultValue) {
        return FieldUtil.toInt(id, defaultValue);
    }

    public Long getLongId() {
        return FieldUtil.toLong(id);
    }

    public Long getLongId(long defaultValue) {
        return FieldUtil.toLong(id, defaultValue);
    }

    public Long getId() {
        return getLongId();
    }

    public void setId(String id) {
        this.id = id;
    }
}
