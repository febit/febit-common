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
package org.febit.lang.modeler;

import jakarta.annotation.Nullable;

import java.io.Serializable;
import java.util.List;

import static org.febit.lang.modeler.Schemas.NOT_A_STRUCT;

public interface Schema extends Serializable {

    static Schema parse(String str) {
        return parse(null, null, str);
    }

    static Schema parse(@Nullable String space, @Nullable String name, String str) {
        return Schemas.parse(space, name, str);
    }

    static Schema parseStruct(String name, String... declares) {
        return Schemas.parseStruct(name, declares);
    }

    SchemaType type();

    String toJavaTypeString();

    String toTypeString();

    @Override
    String toString();

    default boolean isType(SchemaType type) {
        return type().equals(type);
    }

    default boolean isBooleanType() {
        return isType(SchemaType.BOOLEAN);
    }

    default boolean isBytesType() {
        return isType(SchemaType.BYTES);
    }

    default boolean isShortType() {
        return isType(SchemaType.SHORT);
    }

    default boolean isIntType() {
        return isType(SchemaType.INT);
    }

    default boolean isBigintType() {
        return isType(SchemaType.LONG);
    }

    default boolean isFloatType() {
        return isType(SchemaType.FLOAT);
    }

    default boolean isDoubleType() {
        return isType(SchemaType.DOUBLE);
    }

    default boolean isInstantType() {
        return isType(SchemaType.INSTANT);
    }

    default boolean isStringType() {
        return isType(SchemaType.STRING);
    }

    default boolean isOptionalType() {
        return isType(SchemaType.OPTIONAL);
    }

    default boolean isArrayType() {
        return isType(SchemaType.ARRAY);
    }

    default boolean isListType() {
        return isType(SchemaType.LIST);
    }

    default boolean isEnumType() {
        return isType(SchemaType.ENUM);
    }

    default boolean isJsonType() {
        return isType(SchemaType.JSON);
    }

    default boolean isRawType() {
        return isType(SchemaType.RAW);
    }

    default boolean isMapType() {
        return isType(SchemaType.MAP);
    }

    default boolean isStructType() {
        return isType(SchemaType.STRUCT);
    }

    @Nullable
    default String namespace() {
        return null;
    }

    @Nullable
    default String name() {
        return type().getTypeString();
    }

    @Nullable
    default String fullname() {
        return name();
    }

    @Nullable
    default String comment() {
        return null;
    }

    /**
     * If this is a struct, returns the fields in lines string.
     */
    default String toFieldLinesString() {
        throw new UnsupportedOperationException(NOT_A_STRUCT + this);
    }

    /**
     * If this is a struct, returns the Field with the given name.
     *
     * @param name field name
     * @return field
     */
    default Field field(String name) {
        throw new UnsupportedOperationException(NOT_A_STRUCT + this);
    }

    /**
     * If this is a struct, returns the fields in it.
     */
    default List<Field> fields() {
        throw new UnsupportedOperationException(NOT_A_STRUCT + this);
    }

    /**
     * If this is a struct, returns the fields size.
     */
    default int fieldsSize() {
        throw new UnsupportedOperationException(NOT_A_STRUCT + this);
    }

    default Schema keyType() {
        throw new UnsupportedOperationException("Not map optional: " + this);
    }

    /**
     * If this is an array, map or optional, returns its value type.
     */
    default Schema valueType() {
        throw new UnsupportedOperationException("Not an array, map or optional: " + this);
    }

    /**
     * If this is a raw, returns its raw type.
     */
    default String raw() {
        throw new UnsupportedOperationException("Not a raw : " + this);
    }

    interface Field extends Comparable<Field>, Serializable {
        int pos();

        String name();

        Schema schema();

        @Nullable
        String comment();

        @Override
        default int compareTo(Field o) {
            return Integer.compare(pos(), o.pos());
        }
    }

}
