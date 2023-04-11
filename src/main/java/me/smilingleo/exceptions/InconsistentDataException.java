package me.smilingleo.exceptions;

public class InconsistentDataException extends ObjectQueryException {

    public InconsistentDataException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
