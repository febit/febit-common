package org.febit.form;

import org.febit.form.util.FieldUtil;

/**
 *
 * @author zqq90
 */
public class IdForm {

    protected String id;

    public boolean requiredCheck() {
        return id != null && !id.isEmpty();
    }

    public String getStringId() {
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
