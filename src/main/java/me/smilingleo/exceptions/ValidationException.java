package me.smilingleo.exceptions;

public class ValidationException extends WeaverException {

    public ValidationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public ValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

}
