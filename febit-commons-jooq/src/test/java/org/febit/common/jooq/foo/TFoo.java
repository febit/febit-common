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
package org.febit.common.jooq.foo;

import org.febit.common.jooq.ITable;
import org.febit.common.jooq.converter.JsonConverter;
import org.febit.common.jooq.converter.JsonStringConverter;
import org.febit.common.jooq.converter.LocalDateTimeToInstantConverter;
import org.febit.common.jooq.converter.OffsetDateTimeToInstantConverter;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableLike;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"all", "unchecked", "rawtypes", "this-escape"})
public class TFoo extends TableImpl<FooRecord> implements ITable<FooRecord, Long> {

    private static final long serialVersionUID = 1L;

    public static final TFoo FOO = new TFoo();

    @Override
    public Class<FooRecord> getRecordType() {
        return FooRecord.class;
    }

    public final TableField<FooRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false), this, "");
    public final TableField<FooRecord, Boolean> ENABLED = createField(DSL.name("enabled"), SQLDataType.BOOLEAN, this, "");
    public final TableField<FooRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(128).nullable(false), this, "");
    public final TableField<FooRecord, FooStatus> STATUS = createField(DSL.name("status"), SQLDataType.VARCHAR(32).nullable(false), this, "", org.febit.common.jooq.converter.ValuedEnumConverter.forEnum(FooStatus.class));
    public final TableField<FooRecord, String> DESCRIPTION = createField(DSL.name("description"), SQLDataType.VARCHAR(255), this, "");
    public final TableField<FooRecord, LocalDate> DATE = createField(DSL.name("date"), SQLDataType.LOCALDATE, this, "");
    public final TableField<FooRecord, LocalTime> TIME = createField(DSL.name("time"), SQLDataType.LOCALTIME(6), this, "");
    public final TableField<FooRecord, Instant> TIMESTAMP = createField(DSL.name("timestamp"), SQLDataType.LOCALDATETIME(6), this, "", new LocalDateTimeToInstantConverter());
    public final TableField<FooRecord, JsonBean> JSON_VARCHAR = createField(DSL.name("json_varchar"), SQLDataType.JSON, this, "", JsonConverter.forBean(JsonBean.class));
    public final TableField<FooRecord, JsonBean> JSON_TEXT = createField(DSL.name("json_text"), SQLDataType.JSON, this, "", JsonConverter.forBean(JsonBean.class));
    public final TableField<FooRecord, JsonBean> STRING_JSON_BEAN = createField(DSL.name("string_json_bean"), SQLDataType.VARCHAR(255), this, "", JsonStringConverter.forBean(JsonBean.class));
    public final TableField<FooRecord, JsonBean[]> STRING_JSON_BEAN_ARRAY = createField(DSL.name("string_json_bean_array"), SQLDataType.VARCHAR(255), this, "", JsonStringConverter.forBeanArray(JsonBean.class));
    public final TableField<FooRecord, List<JsonBean>> STRING_JSON_BEAN_LIST = createField(DSL.name("string_json_bean_list"), SQLDataType.VARCHAR(255), this, "", JsonStringConverter.forBeanList(JsonBean.class));
    public final TableField<FooRecord, Map<Integer, JsonBean>> STRING_JSON_BEAN_MAP = createField(DSL.name("string_json_bean_map"), SQLDataType.VARCHAR(255), this, "", JsonStringConverter.forBeanMap(Integer.class, JsonBean.class));
    public final TableField<FooRecord, Map<Integer, Boolean>> STRING_JSON_MAP = createField(DSL.name("string_json_map"), SQLDataType.VARCHAR(255), this, "", JsonStringConverter.forBeanMap(Integer.class, Boolean.class));
    public final TableField<FooRecord, String> CREATED_BY = createField(DSL.name("created_by"), SQLDataType.VARCHAR(128), this, "");
    public final TableField<FooRecord, String> UPDATED_BY = createField(DSL.name("updated_by"), SQLDataType.VARCHAR(128), this, "");
    public final TableField<FooRecord, Instant> CREATED_AT = createField(DSL.name("created_at"), SQLDataType.TIMESTAMPWITHTIMEZONE(6).nullable(false).defaultValue(DSL.field(DSL.raw("CURRENT_TIMESTAMP(6)"), SQLDataType.TIMESTAMPWITHTIMEZONE)), this, "", new OffsetDateTimeToInstantConverter());
    public final TableField<FooRecord, Instant> UPDATED_AT = createField(DSL.name("updated_at"), SQLDataType.TIMESTAMPWITHTIMEZONE(6).nullable(false).defaultValue(DSL.field(DSL.raw("CURRENT_TIMESTAMP(6)"), SQLDataType.TIMESTAMPWITHTIMEZONE)), this, "", new OffsetDateTimeToInstantConverter());

    private TFoo(Name alias, Table<FooRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private TFoo(Name alias, Table<FooRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    public TFoo(String alias) {
        this(DSL.name(alias), FOO);
    }

    public TFoo(Name alias) {
        this(alias, FOO);
    }

    public TFoo() {
        this(DSL.name("foo"), null);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : TestH2.TEST_H2;
    }

    @Override
    public UniqueKey<FooRecord> getPrimaryKey() {
        return Keys.PK_FOO;
    }

    @Override
    public TFoo as(String alias) {
        return new TFoo(DSL.name(alias), this);
    }

    @Override
    public TFoo as(Name alias) {
        return new TFoo(alias, this);
    }

    @Override
    public TFoo as(Table<?> alias) {
        return new TFoo(alias.getQualifiedName(), this);
    }

    @Override
    public TFoo rename(String name) {
        return new TFoo(DSL.name(name), null);
    }

    @Override
    public TFoo rename(Name name) {
        return new TFoo(name, null);
    }

    @Override
    public TFoo rename(Table<?> name) {
        return new TFoo(name.getQualifiedName(), null);
    }

    @Override
    public TFoo where(Condition condition) {
        return new TFoo(getQualifiedName(), aliased() ? this : null, null, Internal.condition(this, condition));
    }

    @Override
    public TFoo where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    @Override
    public TFoo where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    @Override
    public TFoo where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    @Override
    @PlainSQL
    public TFoo where(SQL condition) {
        return where(DSL.condition(condition));
    }

    @Override
    @PlainSQL
    public TFoo where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    @Override
    @PlainSQL
    public TFoo where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    @Override
    @PlainSQL
    public TFoo where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    @Override
    public TFoo whereExists(TableLike<?> select) {
        return where(DSL.exists(select));
    }

    @Override
    public TFoo whereNotExists(TableLike<?> select) {
        return where(DSL.notExists(select));
    }

    public Field<Long> pkField() {
        return ID;
    }

    private final List<TableField<FooRecord, ?>> pkExcludedFields = List.of(
            ID,
            ENABLED,
            NAME,
            STATUS,
            DESCRIPTION,
            DATE,
            TIME,
            TIMESTAMP,
            JSON_VARCHAR,
            JSON_TEXT,
            STRING_JSON_BEAN,
            STRING_JSON_BEAN_ARRAY,
            STRING_JSON_BEAN_LIST,
            STRING_JSON_BEAN_MAP,
            STRING_JSON_MAP,
            CREATED_BY,
            UPDATED_BY,
            CREATED_AT,
            UPDATED_AT
    );

    public List<TableField<FooRecord, ?>> pkExcludedFields() {
        return pkExcludedFields;
    }

    public static class Columns {
        public static final String ID = "id";
        public static final String ENABLED = "enabled";
        public static final String NAME = "name";
        public static final String STATUS = "status";
        public static final String DESCRIPTION = "description";
        public static final String DATE = "date";
        public static final String TIME = "time";
        public static final String TIMESTAMP = "timestamp";
        public static final String JSON_VARCHAR = "json_varchar";
        public static final String JSON_TEXT = "json_text";
        public static final String STRING_JSON_BEAN = "string_json_bean";
        public static final String STRING_JSON_BEAN_ARRAY = "string_json_bean_array";
        public static final String STRING_JSON_BEAN_LIST = "string_json_bean_list";
        public static final String STRING_JSON_BEAN_MAP = "string_json_bean_map";
        public static final String STRING_JSON_MAP = "string_json_map";
        public static final String CREATED_BY = "created_by";
        public static final String UPDATED_BY = "updated_by";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
    }
}
