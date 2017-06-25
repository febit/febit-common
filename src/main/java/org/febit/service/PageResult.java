package org.febit.service;

import java.util.List;

/**
 *
 * @author zqq90
 */
public interface PageResult {

    int getPage();

    int getPageSize();

    List getResults();

    void setResults(List results);

    long getTotalSize();

    void setTotalSize(long totalSize);

    int getTotalPage();

    public Object getLinks();

    public void setLinks(Object links);
}
