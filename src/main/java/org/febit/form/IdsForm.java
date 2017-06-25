package org.febit.form;

import org.febit.convert.Convert;

/**
 *
 * @author zqq90
 */
public class IdsForm {

    protected String ids;

    public long[] getIds() {
        return getLongIds();
    }

    public int[] getIntIds() {
        return Convert.toIntArray(ids);
    }

    public long[] getLongIds() {
        if (ids != null) {
            return Convert.toLongArray(ids);
        }
        return null;
    }

    public String[] getStringIds() {
        return Convert.toStringArray(ids);
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public boolean requiredCheck() {
        return ids != null && !ids.isEmpty();
    }
}
