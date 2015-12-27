package org.pandapay.utils.impl;

import org.pandapay.utils.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by LANLI on 2015/12/20.
 */
public class UuidGenerator implements IdGenerator<String>, IdGeneratorConfig {

    private String idType;

    @Override
    public String nextID() {
        return UuidUtils.getTimeUUID().toString();
    }

    @Override
    public void setParams(Map<String, String> params) {
        //no need
    }

    @Override
    public void setIdType(String idType) {
        this.idType = idType;
    }

    @Override
    public String toString() {
        return "ID Type: " + idType;
    }
}
