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
package org.febit.lang.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.febit.lang.Valued;
import org.jspecify.annotations.Nullable;

@Data
@AllArgsConstructor(staticName = "of")
public class Sort {

    private String property;

    @Nullable
    private Direction direction;

    public static Sort asc(String column) {
        return of(column, Direction.ASC);
    }

    public static Sort desc(String column) {
        return of(column, Direction.DESC);
    }

    @JsonIgnore
    public boolean isAsc() {
        return getDirection() == Direction.ASC;
    }

    public Direction getDirection() {
        return direction != null ? direction : Direction.ASC;
    }

    @JsonIgnore
    public boolean isDesc() {
        return getDirection() == Direction.DESC;
    }

    @Override
    public String toString() {
        return this.property + ',' + this.direction.getValue();
    }

    @Getter
    @RequiredArgsConstructor
    public enum Direction implements Valued<String> {
        ASC("asc"),
        DESC("desc"),
        ;

        private final String value;
    }
}
