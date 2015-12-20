package org.pandapay.utils.impl;

import org.pandapay.utils.IdGenerator;

/**
 * Created by LANLI on 2015/12/20.
 */
public class UuidGenerator implements IdGenerator {
    @Override
    public String nextID() {
        return UuidUtils.getTimeUUID().toString();
    }
}
