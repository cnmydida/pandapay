package org.pandapay.utils.impl;

import java.util.Map;

/**
 * Created by LANLI on 2015/12/27.
 */
public interface IdGeneratorConfig {

    void setParams(Map<String, String> params);

    void setIdType(String idType);
}
