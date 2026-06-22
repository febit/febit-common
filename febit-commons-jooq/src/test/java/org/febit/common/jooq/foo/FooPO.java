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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.febit.common.jooq.IEntity;

import java.io.Serializable;
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
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder(
        toBuilder = true,
        builderClassName = "Builder"
)
public class FooPO implements IEntity<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Boolean enabled;
    private String name;
    private FooStatus status;
    private String description;
    private LocalDate date;
    private LocalTime time;
    private Instant timestamp;
    private JsonBean jsonVarchar;
    private JsonBean jsonText;
    private JsonBean stringJsonBean;
    private JsonBean[] stringJsonBeanArray;
    private List<JsonBean> stringJsonBeanList;
    private Map<Integer, JsonBean> stringJsonBeanMap;
    private Map<Integer, Boolean> stringJsonMap;
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;

    public FooPO(FooPO value) {
        this.id = value.id;
        this.enabled = value.enabled;
        this.name = value.name;
        this.status = value.status;
        this.description = value.description;
        this.date = value.date;
        this.time = value.time;
        this.timestamp = value.timestamp;
        this.jsonVarchar = value.jsonVarchar;
        this.jsonText = value.jsonText;
        this.stringJsonBean = value.stringJsonBean;
        this.stringJsonBeanArray = value.stringJsonBeanArray;
        this.stringJsonBeanList = value.stringJsonBeanList;
        this.stringJsonBeanMap = value.stringJsonBeanMap;
        this.stringJsonMap = value.stringJsonMap;
        this.createdBy = value.createdBy;
        this.updatedBy = value.updatedBy;
        this.createdAt = value.createdAt;
        this.updatedAt = value.updatedAt;
    }

    @Id
    @Column(name = "id", nullable = false)
    @NotNull
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "enabled")
    public Boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Column(name = "name", nullable = false, length = 128)
    @NotNull
    @Size(max = 128)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "status", nullable = false, length = 32)
    @NotNull
    public FooStatus getStatus() {
        return this.status;
    }

    public void setStatus(FooStatus status) {
        this.status = status;
    }

    @Column(name = "description", length = 255)
    @Size(max = 255)
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "date")
    public LocalDate getDate() {
        return this.date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Column(name = "time", precision = 6)
    public LocalTime getTime() {
        return this.time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    @Column(name = "timestamp", precision = 6)
    public Instant getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Column(name = "json_varchar")
    public JsonBean getJsonVarchar() {
        return this.jsonVarchar;
    }

    public void setJsonVarchar(JsonBean jsonVarchar) {
        this.jsonVarchar = jsonVarchar;
    }

    @Column(name = "json_text")
    public JsonBean getJsonText() {
        return this.jsonText;
    }

    public void setJsonText(JsonBean jsonText) {
        this.jsonText = jsonText;
    }

    @Column(name = "string_json_bean", length = 255)
    public JsonBean getStringJsonBean() {
        return this.stringJsonBean;
    }

    public void setStringJsonBean(JsonBean stringJsonBean) {
        this.stringJsonBean = stringJsonBean;
    }

    @Column(name = "string_json_bean_array", length = 255)
    public JsonBean[] getStringJsonBeanArray() {
        return this.stringJsonBeanArray;
    }

    public void setStringJsonBeanArray(JsonBean[] stringJsonBeanArray) {
        this.stringJsonBeanArray = stringJsonBeanArray;
    }

    @Column(name = "string_json_bean_list", length = 255)
    public List<JsonBean> getStringJsonBeanList() {
        return this.stringJsonBeanList;
    }

    public void setStringJsonBeanList(List<JsonBean> stringJsonBeanList) {
        this.stringJsonBeanList = stringJsonBeanList;
    }

    @Column(name = "string_json_bean_map", length = 255)
    public Map<Integer, JsonBean> getStringJsonBeanMap() {
        return this.stringJsonBeanMap;
    }

    public void setStringJsonBeanMap(Map<Integer, JsonBean> stringJsonBeanMap) {
        this.stringJsonBeanMap = stringJsonBeanMap;
    }

    @Column(name = "string_json_map", length = 255)
    public Map<Integer, Boolean> getStringJsonMap() {
        return this.stringJsonMap;
    }

    public void setStringJsonMap(Map<Integer, Boolean> stringJsonMap) {
        this.stringJsonMap = stringJsonMap;
    }

    @Column(name = "created_by", length = 128)
    @Size(max = 128)
    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name = "updated_by", length = 128)
    @Size(max = 128)
    public String getUpdatedBy() {
        return this.updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Column(name = "created_at", nullable = false, precision = 6)
    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Column(name = "updated_at", nullable = false, precision = 6)
    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public Long id() {
        return getId();
    }

    @Override
    public FooRecord toRecord() {
        return new FooRecord(this);
    }

    public void merge(FooRecord record) {
        if (record == null) {
            return;
        }
        setId(record.getId());
        setEnabled(record.getEnabled());
        setName(record.getName());
        setStatus(record.getStatus());
        setDescription(record.getDescription());
        setDate(record.getDate());
        setTime(record.getTime());
        setTimestamp(record.getTimestamp());
        setJsonVarchar(record.getJsonVarchar());
        setJsonText(record.getJsonText());
        setStringJsonBean(record.getStringJsonBean());
        setStringJsonBeanArray(record.getStringJsonBeanArray());
        setStringJsonBeanList(record.getStringJsonBeanList());
        setStringJsonBeanMap(record.getStringJsonBeanMap());
        setStringJsonMap(record.getStringJsonMap());
        setCreatedBy(record.getCreatedBy());
        setUpdatedBy(record.getUpdatedBy());
        setCreatedAt(record.getCreatedAt());
        setUpdatedAt(record.getUpdatedAt());
    }
}
