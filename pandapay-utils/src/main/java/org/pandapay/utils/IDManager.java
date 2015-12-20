package org.pandapay.utils;

/**
 * Created by LANLI on 2015/12/20.
 */
public interface IdManager {

    IdGenerator getIdGenerator(String idType);
}
