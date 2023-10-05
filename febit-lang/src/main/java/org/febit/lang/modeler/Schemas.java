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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.febit.lang.util.StringWalker;

import java.util.regex.Pattern;

import static org.febit.lang.modeler.SchemaType.STRING;

@UtilityClass
public class Schemas {

    static final String NOT_A_STRUCT = "Not a struct: ";
    private static final String TYPE_NAME_END_CHARS = "\r\n \t\f\b<>[],:;+=#";
    private static final String LINE_BREAKERS = "\r\n";
    private static final String LINE_BREAKERS_REPLACE = "  ";
    private static final Pattern NAME_PATTERN = Pattern.compile("^[_a-zA-Z][_a-zA-Z0-9]{0,64}$");

    static void checkName(String name) {
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Illegal name: " + name);
        }
    }

    @Nullable
    static String escapeForLineComment(@Nullable String remark) {
        if (remark == null) {
            return null;
        }
        return StringUtils.replaceChars(remark, LINE_BREAKERS, LINE_BREAKERS_REPLACE);
    }

    private static boolean isTypeNameEnding(char c) {
        return TYPE_NAME_END_CHARS.indexOf(c) >= 0;
    }

    public static Schema ofPrimitive(SchemaType type) {
        switch (type) {
            case STRING:
            case BYTES:
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case BOOLEAN:
            case DATE:
            case TIME:
            case DATETIME:
            case DATETIME_ZONED:
            case INSTANT:
                return PrimitiveSchema.of(type);
            case LIST:
            case ENUM:
            case JSON:
            case RAW:
            case STRUCT:
            case ARRAY:
            case MAP:
            case OPTIONAL:
            default:
                throw new IllegalArgumentException("Can't create Schema for type: " + type);
        }
    }

    public static Schema ofArray(Schema valueType) {
        return ElementSchema.of(SchemaType.ARRAY, valueType);
    }

    public static Schema ofList(Schema valueType) {
        return ElementSchema.of(SchemaType.LIST, valueType);
    }

    public static Schema ofEnum(Schema valueType) {
        return ElementSchema.of(SchemaType.ENUM, valueType);
    }

    public static Schema ofOptional(Schema valueType) {
        return ElementSchema.of(SchemaType.OPTIONAL, valueType);
    }

    public static Schema ofJson(Schema valueType) {
        return ElementSchema.of(SchemaType.JSON, valueType);
    }

    public static Schema ofRaw(String raw) {
        return RawSchema.of(raw);
    }

    public static Schema ofMap(Schema valueType) {
        return ofMap(Schemas.ofPrimitive(STRING), valueType);
    }

    public static Schema ofMap(Schema keyType, Schema valueType) {
        return MapSchema.of(keyType, valueType);
    }

    static Schema parseStruct(String name, String... declares) {
        var builder = newStruct();
        builder.name(name);

        for (var line : declares) {
            line = line.trim();
            if (line.isEmpty()
                    || line.charAt(0) == '#') {
                continue;
            }
            parseField(name, line, builder);
        }
        return builder.build();
    }

    public static StructSchemaBuilder newStruct() {
        return new StructSchemaBuilder();
    }

    private static Schema readElementSchema(SchemaType type, @Nullable String space, @Nullable String name, StringWalker walker) {
        walker.skipBlanks();

        var colon = walker.peek() == ':';
        if (colon) {
            walker.jump(1);
        } else {
            walker.requireAndJumpChar('<');
        }

        walker.skipBlanks();
        var elementType = readType(buildNamespace(space, name), "item", walker);
        walker.skipBlanks();

        if (!colon) {
            walker.requireAndJumpChar('>');
        }

        return ElementSchema.of(type, elementType);
    }

    private static Schema readMapType(@Nullable String space, @Nullable String name, StringWalker walker) {
        walker.skipBlanks();
        walker.requireAndJumpChar('<');
        walker.skipBlanks();
        var keyType = readType(buildNamespace(space, name), "key", walker);
        walker.skipBlanks();
        walker.requireAndJumpChar(',');
        walker.skipBlanks();
        var valueType = readType(buildNamespace(space, name), "value", walker);
        walker.requireAndJumpChar('>');
        return MapSchema.of(keyType, valueType);
    }

    private static Schema readStructType(@Nullable String space, @Nullable String name, StringWalker walker) {
        if (name == null || name.isEmpty()) {
            name = "struct";
        }

        walker.skipBlanks();
        walker.requireAndJumpChar('<');
        walker.skipBlanks();

        var builder = newStruct();
        builder.namespace(space);
        builder.name(name);

        var childSpace = buildNamespace(space, name);
        while (!walker.isEnd() && walker.peek() != '>') {
            walker.skipBlanks();
            var fieldName = walker.readTo(':', false).trim();
            var fieldType = readType(childSpace, fieldName, walker);
            builder.field(fieldName, fieldType, null);
            walker.skipBlanks();
            if (walker.isEnd() || walker.peek() != ',') {
                break;
            }
            // jump ','
            walker.jump(1);
            walker.skipBlanks();
        }
        walker.skipBlanks();
        walker.requireAndJumpChar('>');
        return builder.build();
    }

    @Nullable
    private static String buildNamespace(@Nullable String space, @Nullable String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        if (space == null || space.isEmpty()) {
            return name;
        }
        return space + '.' + name;
    }

    static Schema parse(@Nullable String space, @Nullable String name, String str) {
        var walker = new StringWalker(str);
        return readType(space, name, walker);
    }

    private static void parseField(String space, String line, StructSchemaBuilder builder) {
        var walker = new StringWalker(line);
        walker.skipBlanks();
        var schema = readType(space, "_col" + builder.fieldsSize(), walker);
        walker.skipBlanks();
        var name = walker.readUntilBlanks().trim();
        walker.skipBlanks();
        String comment = null;
        if (!walker.isEnd() && walker.peek() == '#') {
            comment = walker.readToEnd().substring(1).trim();
            if (comment.isEmpty()) {
                comment = null;
            }
        }
        walker.skipBlanks();
        if (!walker.isEnd()) {
            throw new IllegalArgumentException("Invalid content: " + walker.readToEnd());
        }
        builder.field(name, schema, comment);
    }

    private static Schema readType(@Nullable String space, @Nullable String name, StringWalker walker) {
        walker.skipBlanks();
        var typeName = walker.readToFlag(Schemas::isTypeNameEnding, true);
        switch (typeName.toLowerCase()) {
            case "short":
            case "int16":
            case "smallint":
                return ofPrimitive(SchemaType.SHORT);
            case "int":
            case "int32":
            case "integer":
                return ofPrimitive(SchemaType.INT);
            case "long":
            case "int64":
            case "bigint":
                return ofPrimitive(SchemaType.LONG);
            case "string":
            case "varchar":
            case "text":
                return ofPrimitive(SchemaType.STRING);
            case "bool":
            case "boolean":
                return ofPrimitive(SchemaType.BOOLEAN);
            case "bytes":
                return ofPrimitive(SchemaType.BYTES);
            case "float":
                return ofPrimitive(SchemaType.FLOAT);
            case "double":
                return ofPrimitive(SchemaType.DOUBLE);
            case "date":
            case "localdate":
                return ofPrimitive(SchemaType.DATE);
            case "time":
            case "localtime":
                return ofPrimitive(SchemaType.TIME);
            case "instant":
                return ofPrimitive(SchemaType.INSTANT);
            case "datetime":
            case "timestamp":
            case "localdatetime":
                return ofPrimitive(SchemaType.DATETIME);
            case "timestamptz":
            case "datetimetz":
            case "zoneddatetime":
            case "datetime_zoned":
            case "datetime_with_timezone":
            case "timestamp_with_timezone":
                return ofPrimitive(SchemaType.DATETIME_ZONED);
            case "optional":
                return readElementSchema(SchemaType.OPTIONAL, space, name, walker);
            case "json":
                return readElementSchema(SchemaType.JSON, space, name, walker);
            case "enum":
                return readElementSchema(SchemaType.ENUM, space, name, walker);
            case "array":
                return readElementSchema(SchemaType.ARRAY, space, name, walker);
            case "list":
                return readElementSchema(SchemaType.LIST, space, name, walker);
            case "map":
                return readMapType(space, name, walker);
            case "struct":
                return readStructType(space, name, walker);
            default:
                return RawSchema.of(typeName);
        }
    }

    @Getter
    @EqualsAndHashCode
    @Accessors(fluent = true)
    @RequiredArgsConstructor(staticName = "of")
    private static class PrimitiveSchema implements Schema {
        private static final long serialVersionUID = 1L;

        private final SchemaType type;

        @Override
        public String toJavaTypeString() {
            return type.toJavaTypeString();
        }

        @Override
        public String toTypeString() {
            return type.toTypeString();
        }

        @Override
        public String toString() {
            return toTypeString();
        }
    }

    @Getter
    @EqualsAndHashCode
    @Accessors(fluent = true)
    @RequiredArgsConstructor(staticName = "of")
    private static class RawSchema implements Schema {
        private static final long serialVersionUID = 1L;

        private final String raw;

        @Override
        public SchemaType type() {
            return SchemaType.RAW;
        }

        @Override
        public String toJavaTypeString() {
            return raw;
        }

        @Override
        public String toTypeString() {
            return raw;
        }

        @Override
        public String toString() {
            return toTypeString();
        }
    }

    @Getter
    @EqualsAndHashCode
    @Accessors(fluent = true)
    @RequiredArgsConstructor(staticName = "of")
    private static class ElementSchema implements Schema {
        private static final long serialVersionUID = 1L;

        private final SchemaType type;
        private final Schema valueType;

        @Override
        public String toJavaTypeString() {
            var valueTypeStr = valueType.toJavaTypeString();
            switch (type) {
                case ARRAY:
                    return valueTypeStr + "[]";
                case ENUM:
                case OPTIONAL:
                    return valueTypeStr;
                default:
                    return type.toJavaTypeString() + '<'
                            + valueType.toJavaTypeString()
                            + '>';
            }
        }

        @Override
        public String toTypeString() {
            return type.toTypeString() + '<'
                    + valueType.toTypeString()
                    + '>';
        }

        @Override
        public String toString() {
            return toTypeString();
        }
    }

    @Getter
    @EqualsAndHashCode
    @Accessors(fluent = true)
    @RequiredArgsConstructor(staticName = "of")
    private static class MapSchema implements Schema {
        private static final long serialVersionUID = 1L;

        private final Schema keyType;
        private final Schema valueType;

        @Override
        public SchemaType type() {
            return SchemaType.MAP;
        }

        @Override
        public String toJavaTypeString() {
            return type().toJavaTypeString() + '<'
                    + keyType.toJavaTypeString()
                    + ", "
                    + valueType.toJavaTypeString()
                    + '>';
        }

        @Override
        public String toTypeString() {
            return type().toTypeString() + '<'
                    + keyType.toTypeString()
                    + ','
                    + valueType.toTypeString()
                    + '>';
        }

        @Override
        public String toString() {
            return toTypeString();
        }
    }
}
