package org.febit.form;

import java.util.List;
import org.febit.vtor.Vtor;

/**
 *
 * @author zqq90
 * @param <E>
 */
public interface AddForm<E> extends BaseForm {

    E createAdded(int prefile);

    List<Vtor> getVtors();
}
