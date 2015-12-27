package org.pandapay.utils.impl;

import org.pandapay.utils.IdGenerator;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by LANLI on 2015/12/27.
 */
public class IncGenerator implements IdGenerator<Long>, IdGeneratorConfig {

    private AtomicLong currentId = new AtomicLong(0L);
    private String idType;

    @Override
    public Long nextID() {
        return currentId.incrementAndGet();
    }

    @Override
    public void setParams(Map<String, String> params) {

        if (params == null) return;

        String initialValueStr = params.get("initialValue");
        if (initialValueStr != null) {
            try {
                Long initialValue = Long.valueOf(initialValueStr);
                currentId.set(initialValue);
            } catch (NumberFormatException e) {
                ;
            }
        }

    }

    @Override
    public void setIdType(String idType) {
        this.idType = idType;
    }

    @Override
    public String toString() {
        return "ID Type: " + idType + " -- CurrentValue: " + currentId.get();
    }
}
