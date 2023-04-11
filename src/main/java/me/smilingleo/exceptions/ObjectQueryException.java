package me.smilingleo.exceptions;

public class ObjectQueryException extends WeaverException {

    public ObjectQueryException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

}
