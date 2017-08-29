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

/**
 *
 * @author zqq
 */
public class PageForm {

    protected int page;
    protected int limit;
    // default is asc, if start with '-' is desc
    protected String[] order;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page > 1 ? page : 1;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Order exportOrder() {
        Order dist = new Order();
        exportTo(dist);
        return dist;
    }

    public void exportTo(final Order dist) {
        if (this.order == null) {
            return;
        }
        for (String field : this.order) {
            field = field.trim();
            if (field.isEmpty()) {
                continue;
            }
            boolean asc = field.charAt(0) != '-';
            if (!asc) {
                field = field.substring(1).trim();
                if (field.isEmpty()) {
                    continue;
                }
            }
            dist.add(field, asc);
        }
    }

    @Override
    public String toString() {
        return "Page{" + "page=" + page + ", limit=" + limit + '}';
    }
}
