package org.febit.form;

import org.febit.form.util.FieldUtil;

/**
 *
 * @author zqq90
 */
public class SimpleModifyForm extends IdForm {

    protected String value;

    @Override
    public boolean requiredCheck() {
        return super.requiredCheck()
                && this.value != null;
    }

    public boolean getBoolValue() {
        return FieldUtil.toBool(value, false);
    }

    public Short getShortValue() {
        return FieldUtil.toShort(value);
    }

    public short getShortValue(short defaultValue) {
        return FieldUtil.toShort(value, defaultValue);
    }

    public int getIntValue() {
        return FieldUtil.toInt(value);
    }

    public int getIntValue(int defaultValue) {
        return FieldUtil.toInt(value, defaultValue);
    }

    public Integer getIntegerValue() {
        return FieldUtil.toInteger(value);
    }

    public Integer getIntegerValue(Integer defaultValue) {
        return FieldUtil.toInteger(value, defaultValue);
    }

    public Long getLongValue() {
        return FieldUtil.toLong(value);
    }

    public Double getDoubleValue() {
        return FieldUtil.toDouble(value);
    }

    public Double getDoubleValue(Double defaultValue) {
        return FieldUtil.toDouble(value, defaultValue);
    }

    public Long getLongValue(long defaultValue) {
        return FieldUtil.toLong(value, defaultValue);
    }

    public String getStringValue() {
        return value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
