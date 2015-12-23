package org.pandapay.utils;

import java.util.Map;

/**
 * Created by LANLI on 2015/12/20.
 */
public interface IdGenerator<T> {

    T nextID();

    void setIdType(String idType);

    void setParams(Map<String, String> params);
}
