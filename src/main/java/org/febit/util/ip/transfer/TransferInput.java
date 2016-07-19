// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util.ip.transfer;

/**
 *
 * @author zqq90
 */
public interface TransferInput {

    long getFrom();

    long getTo();

    String getIsp();

    String getCountry();

    String getProvince();

    String getCity();

}
