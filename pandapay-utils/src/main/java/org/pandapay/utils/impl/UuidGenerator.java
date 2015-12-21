package org.pandapay.utils.impl;

import org.pandapay.utils.IdGenerator;
import org.springframework.stereotype.Component;

/**
 * Created by LANLI on 2015/12/20.
 */
@Component("uuid")
public class UuidGenerator implements IdGenerator {
    @Override
    public String nextID() {
        return UuidUtils.getTimeUUID().toString();
    }
}
