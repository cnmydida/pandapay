package org.pandapay.utils;

/**
 * Created by LANLI on 2015/12/27.
 */
public enum IdGeneratorType {

    INC("INC"), UUID("UUID"), JDBC("JDBC");

    private final String value;

    IdGeneratorType(String input) {
        this.value = input;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
