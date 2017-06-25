package org.febit.form;

import java.util.List;
import java.util.Map;
import org.febit.vtor.Vtor;

/**
 *
 * @author zqq90
 * @param <E>
 * @param <I>
 */
public interface ModifyForm<E, I> extends BaseForm {

    E updateModify(E model, int prefile);

    Map<String, Object> modifyMap(final int profile);

    I id();

    List<Vtor> getVtors();
}
