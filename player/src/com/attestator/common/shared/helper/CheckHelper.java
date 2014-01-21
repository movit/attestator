package com.attestator.common.shared.helper;

import java.util.Collection;

public class CheckHelper {
    public static void throwIfNull(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("null not allowed");
        }
    }

    public static void throwIfNull(Object value, String valueName) {
        if (value == null) {
            throw new IllegalArgumentException("[" + valueName + "] should be not null");
        }
    }
    
    public static <T> void throwIfNullOrEmpty(T[] value, String valueName) {
        if (value == null) {
            throw new IllegalArgumentException("[" + valueName + "] should be not null");
        }
        if (NullHelper.isEmptyOrNull(value)) {
            throw new IllegalArgumentException("[" + valueName + "] should be not empty");
        }
    }
    
    public static void throwIfNullOrEmpty(Collection<?> value, String valueName) {
        if (value == null) {
            throw new IllegalArgumentException("[" + valueName + "] should be not null");
        }
        if (NullHelper.isEmptyOrNull(value)) {
            throw new IllegalArgumentException("[" + valueName + "] should be not empty");
        }
    }

    public static void throwIfNullOrEmpty(String value, String valueName) {
        if (value == null) {
            throw new IllegalArgumentException("[" + valueName + "] should be not null");
        }
        if (StringHelper.isEmptyOrNull(value)) {
            throw new IllegalArgumentException("[" + valueName + "] should be not empty");
        }
    }
    
    public static void throwIfNullOrNotImplement(Object obj, Class<?> clazz, String objName) {
        if (obj == null) {
            throw new IllegalArgumentException("[" + objName + "] should be not null");
        }
        if (!clazz.isAssignableFrom(obj.getClass())) {
            throw new IllegalArgumentException("[" + objName + "] should extend or implement " + clazz.getName());
        }
    }
}
