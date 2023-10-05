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

import lombok.experimental.UtilityClass;

import static org.febit.lang.modeler.SchemaType.BOOLEAN;
import static org.febit.lang.modeler.SchemaType.BYTES;
import static org.febit.lang.modeler.SchemaType.DATE;
import static org.febit.lang.modeler.SchemaType.DATETIME;
import static org.febit.lang.modeler.SchemaType.DATETIME_ZONED;
import static org.febit.lang.modeler.SchemaType.DOUBLE;
import static org.febit.lang.modeler.SchemaType.FLOAT;
import static org.febit.lang.modeler.SchemaType.INSTANT;
import static org.febit.lang.modeler.SchemaType.INT;
import static org.febit.lang.modeler.SchemaType.SHORT;
import static org.febit.lang.modeler.SchemaType.STRING;
import static org.febit.lang.modeler.SchemaType.TIME;
import static org.febit.lang.modeler.Schemas.ofPrimitive;

@UtilityClass
class TestSchemas {

    static final Schema S_INT = ofPrimitive(INT);
    static final Schema S_LONG = ofPrimitive(SchemaType.LONG);
    static final Schema S_STRING = ofPrimitive(STRING);
    static final Schema S_BOOLEAN = ofPrimitive(BOOLEAN);
    static final Schema S_SHORT = ofPrimitive(SHORT);
    static final Schema S_BYTES = ofPrimitive(BYTES);
    static final Schema S_FLOAT = ofPrimitive(FLOAT);
    static final Schema S_DOUBLE = ofPrimitive(DOUBLE);
    static final Schema S_INSTANT = ofPrimitive(INSTANT);
    static final Schema S_DATE = ofPrimitive(DATE);
    static final Schema S_TIME = ofPrimitive(TIME);
    static final Schema S_DATETIME = ofPrimitive(DATETIME);
    static final Schema S_DATETIME_ZONED = ofPrimitive(DATETIME_ZONED);

    static final Schema S_OPTIONAL_STRING = Schemas.ofOptional(S_STRING);
    static final Schema S_OPTIONAL_INSTANT = Schemas.ofOptional(S_INSTANT);
    static final Schema S_LIST_STRING = Schemas.ofList(S_STRING);
    static final Schema S_LIST_INSTANT = Schemas.ofList(S_INSTANT);
    static final Schema S_MAP_STRING = Schemas.ofMap(S_STRING);
    static final Schema S_MAP_INSTANT = Schemas.ofMap(S_INSTANT);

    static final Schema S_ENUM_STR = Schemas.ofEnum(S_STRING);
    static final Schema S_ARRAY_STR = Schemas.ofArray(S_STRING);
    static final Schema S_JSON_STR = Schemas.ofJson(S_STRING);
    static final Schema S_RAW_STR = Schemas.ofRaw("org.febit.demo.model.User");

    static final Schema S_STRUCT_SIMPLE = Schemas.newStruct()
            .field("id", S_INT)
            .field("name", S_STRING)
            .build();
}
