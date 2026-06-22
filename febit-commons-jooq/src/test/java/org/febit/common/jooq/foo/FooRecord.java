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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"all", "unchecked", "rawtypes", "this-escape"})
@Entity
@Table(
        name = "foo",
        schema = "test_h2"
)
public class FooRecord extends UpdatableRecordImpl<FooRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>test_h2.foo.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>test_h2.foo.id</code>.
     */
    @Id
    @Column(name = "id", nullable = false)
    @NotNull
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>test_h2.foo.enabled</code>.
     */
    public void setEnabled(Boolean value) {
        set(1, value);
    }

    /**
     * Getter for <code>test_h2.foo.enabled</code>.
     */
    @Column(name = "enabled")
    public Boolean getEnabled() {
        return (Boolean) get(1);
    }

    /**
     * Setter for <code>test_h2.foo.name</code>.
     */
    public void setName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>test_h2.foo.name</code>.
     */
    @Column(name = "name", nullable = false, length = 128)
    @NotNull
    @Size(max = 128)
    public String getName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>test_h2.foo.status</code>.
     */
    public void setStatus(FooStatus value) {
        set(3, value);
    }

    /**
     * Getter for <code>test_h2.foo.status</code>.
     */
    @Column(name = "status", nullable = false, length = 32)
    @NotNull
    public FooStatus getStatus() {
        return (FooStatus) get(3);
    }

    /**
     * Setter for <code>test_h2.foo.description</code>.
     */
    public void setDescription(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>test_h2.foo.description</code>.
     */
    @Column(name = "description", length = 255)
    @Size(max = 255)
    public String getDescription() {
        return (String) get(4);
    }

    /**
     * Setter for <code>test_h2.foo.date</code>.
     */
    public void setDate(LocalDate value) {
        set(5, value);
    }

    /**
     * Getter for <code>test_h2.foo.date</code>.
     */
    @Column(name = "date")
    public LocalDate getDate() {
        return (LocalDate) get(5);
    }

    /**
     * Setter for <code>test_h2.foo.time</code>.
     */
    public void setTime(LocalTime value) {
        set(6, value);
    }

    /**
     * Getter for <code>test_h2.foo.time</code>.
     */
    @Column(name = "time", precision = 6)
    public LocalTime getTime() {
        return (LocalTime) get(6);
    }

    /**
     * Setter for <code>test_h2.foo.timestamp</code>.
     */
    public void setTimestamp(Instant value) {
        set(7, value);
    }

    /**
     * Getter for <code>test_h2.foo.timestamp</code>.
     */
    @Column(name = "timestamp", precision = 6)
    public Instant getTimestamp() {
        return (Instant) get(7);
    }

    /**
     * Setter for <code>test_h2.foo.json_varchar</code>.
     */
    public void setJsonVarchar(JsonBean value) {
        set(8, value);
    }

    /**
     * Getter for <code>test_h2.foo.json_varchar</code>.
     */
    @Column(name = "json_varchar")
    public JsonBean getJsonVarchar() {
        return (JsonBean) get(8);
    }

    /**
     * Setter for <code>test_h2.foo.json_text</code>.
     */
    public void setJsonText(JsonBean value) {
        set(9, value);
    }

    /**
     * Getter for <code>test_h2.foo.json_text</code>.
     */
    @Column(name = "json_text")
    public JsonBean getJsonText() {
        return (JsonBean) get(9);
    }

    /**
     * Setter for <code>test_h2.foo.string_json_bean</code>.
     */
    public void setStringJsonBean(JsonBean value) {
        set(10, value);
    }

    /**
     * Getter for <code>test_h2.foo.string_json_bean</code>.
     */
    @Column(name = "string_json_bean", length = 255)
    public JsonBean getStringJsonBean() {
        return (JsonBean) get(10);
    }

    /**
     * Setter for <code>test_h2.foo.string_json_bean_array</code>.
     */
    public void setStringJsonBeanArray(JsonBean[] value) {
        set(11, value);
    }

    /**
     * Getter for <code>test_h2.foo.string_json_bean_array</code>.
     */
    @Column(name = "string_json_bean_array", length = 255)
    public JsonBean[] getStringJsonBeanArray() {
        return (JsonBean[]) get(11);
    }

    /**
     * Setter for <code>test_h2.foo.string_json_bean_list</code>.
     */
    public void setStringJsonBeanList(List<JsonBean> value) {
        set(12, value);
    }

    /**
     * Getter for <code>test_h2.foo.string_json_bean_list</code>.
     */
    @Column(name = "string_json_bean_list", length = 255)
    public List<JsonBean> getStringJsonBeanList() {
        return (List<JsonBean>) get(12);
    }

    /**
     * Setter for <code>test_h2.foo.string_json_bean_map</code>.
     */
    public void setStringJsonBeanMap(Map<Integer, JsonBean> value) {
        set(13, value);
    }

    /**
     * Getter for <code>test_h2.foo.string_json_bean_map</code>.
     */
    @Column(name = "string_json_bean_map", length = 255)
    public Map<Integer, JsonBean> getStringJsonBeanMap() {
        return (Map<Integer, JsonBean>) get(13);
    }

    /**
     * Setter for <code>test_h2.foo.string_json_map</code>.
     */
    public void setStringJsonMap(Map<Integer, Boolean> value) {
        set(14, value);
    }

    /**
     * Getter for <code>test_h2.foo.string_json_map</code>.
     */
    @Column(name = "string_json_map", length = 255)
    public Map<Integer, Boolean> getStringJsonMap() {
        return (Map<Integer, Boolean>) get(14);
    }

    /**
     * Setter for <code>test_h2.foo.created_by</code>.
     */
    public void setCreatedBy(String value) {
        set(15, value);
    }

    /**
     * Getter for <code>test_h2.foo.created_by</code>.
     */
    @Column(name = "created_by", length = 128)
    @Size(max = 128)
    public String getCreatedBy() {
        return (String) get(15);
    }

    /**
     * Setter for <code>test_h2.foo.updated_by</code>.
     */
    public void setUpdatedBy(String value) {
        set(16, value);
    }

    /**
     * Getter for <code>test_h2.foo.updated_by</code>.
     */
    @Column(name = "updated_by", length = 128)
    @Size(max = 128)
    public String getUpdatedBy() {
        return (String) get(16);
    }

    /**
     * Setter for <code>test_h2.foo.created_at</code>.
     */
    public void setCreatedAt(Instant value) {
        set(17, value);
    }

    /**
     * Getter for <code>test_h2.foo.created_at</code>.
     */
    @Column(name = "created_at", nullable = false, precision = 6)
    public Instant getCreatedAt() {
        return (Instant) get(17);
    }

    /**
     * Setter for <code>test_h2.foo.updated_at</code>.
     */
    public void setUpdatedAt(Instant value) {
        set(18, value);
    }

    /**
     * Getter for <code>test_h2.foo.updated_at</code>.
     */
    @Column(name = "updated_at", nullable = false, precision = 6)
    public Instant getUpdatedAt() {
        return (Instant) get(18);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached FooRecord
     */
    public FooRecord() {
        super(TFoo.FOO);
    }

    /**
     * Create a detached, initialised FooRecord
     */
    public FooRecord(Long id, Boolean enabled, String name, FooStatus status, String description, LocalDate date, LocalTime time, Instant timestamp, JsonBean jsonVarchar, JsonBean jsonText, JsonBean stringJsonBean, JsonBean[] stringJsonBeanArray, List<JsonBean> stringJsonBeanList, Map<Integer, JsonBean> stringJsonBeanMap, Map<Integer, Boolean> stringJsonMap, String createdBy, String updatedBy, Instant createdAt, Instant updatedAt) {
        super(TFoo.FOO);

        setId(id);
        setEnabled(enabled);
        setName(name);
        setStatus(status);
        setDescription(description);
        setDate(date);
        setTime(time);
        setTimestamp(timestamp);
        setJsonVarchar(jsonVarchar);
        setJsonText(jsonText);
        setStringJsonBean(stringJsonBean);
        setStringJsonBeanArray(stringJsonBeanArray);
        setStringJsonBeanList(stringJsonBeanList);
        setStringJsonBeanMap(stringJsonBeanMap);
        setStringJsonMap(stringJsonMap);
        setCreatedBy(createdBy);
        setUpdatedBy(updatedBy);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        resetTouchedOnNotNull();
    }

    /**
     * Create a detached, initialised FooRecord
     */
    public FooRecord(FooPO value) {
        super(TFoo.FOO);

        if (value != null) {
            setId(value.getId());
            setEnabled(value.getEnabled());
            setName(value.getName());
            setStatus(value.getStatus());
            setDescription(value.getDescription());
            setDate(value.getDate());
            setTime(value.getTime());
            setTimestamp(value.getTimestamp());
            setJsonVarchar(value.getJsonVarchar());
            setJsonText(value.getJsonText());
            setStringJsonBean(value.getStringJsonBean());
            setStringJsonBeanArray(value.getStringJsonBeanArray());
            setStringJsonBeanList(value.getStringJsonBeanList());
            setStringJsonBeanMap(value.getStringJsonBeanMap());
            setStringJsonMap(value.getStringJsonMap());
            setCreatedBy(value.getCreatedBy());
            setUpdatedBy(value.getUpdatedBy());
            setCreatedAt(value.getCreatedAt());
            setUpdatedAt(value.getUpdatedAt());
            resetTouchedOnNotNull();
        }
    }

    public static FooRecord fromPojo(FooPO po) {
        FooRecord record = new FooRecord();
        record.from(po);
        return record;
    }
}
