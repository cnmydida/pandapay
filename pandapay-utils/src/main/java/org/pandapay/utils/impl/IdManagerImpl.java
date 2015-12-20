package org.pandapay.utils.impl;

import org.pandapay.utils.IdGenerator;
import org.pandapay.utils.IdManager;

/**
 * Created by LANLI on 2015/12/20.
 */
public class IdManagerImpl implements IdManager {
    @Override
    public IdGenerator getIdGenerator(String idType) {
        return new UuidGenerator();
    }
}
