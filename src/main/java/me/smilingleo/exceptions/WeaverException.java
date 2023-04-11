package me.smilingleo.exceptions;

public abstract class WeaverException extends RuntimeException {

    private ErrorCode errorCode;

    public WeaverException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public WeaverException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
