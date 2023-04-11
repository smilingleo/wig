package me.smilingleo.utils;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;

public class Asserts {

    public static void assertTrue(boolean condition, ErrorCode errorCode, String errorMsg) {
        if (!condition) {
            throw new ValidationException(errorCode, errorMsg);
        }
    }

    public static void assertNotNull(Object input, String errorMsg) {
        assertTrue(input != null, ErrorCode.InvalidValue, errorMsg);
    }

    public static void assertType(Object input, Class type, String errorMsg) {
        assertTrue(type.isAssignableFrom(input.getClass()), ErrorCode.InvalidValue, errorMsg);
    }
}
