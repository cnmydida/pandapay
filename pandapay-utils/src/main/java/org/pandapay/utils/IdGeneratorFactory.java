package org.pandapay.utils;

/**
 * Created by LANLI on 2015/12/27.
 */
public interface IdGeneratorFactory {

    IdGenerator getIdGenerator(IdGeneratorType idGeneratorType);
}
