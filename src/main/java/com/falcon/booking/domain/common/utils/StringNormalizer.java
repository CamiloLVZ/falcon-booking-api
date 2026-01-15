package com.falcon.booking.domain.common.utils;

public final class StringNormalizer {
    private StringNormalizer() {}

    public static String normalize (String value){
        value = value == null ? null : value.trim().toUpperCase();
        return value;
    }
}
