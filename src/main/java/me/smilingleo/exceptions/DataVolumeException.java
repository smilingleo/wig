package me.smilingleo.exceptions;

public class DataVolumeException extends ObjectQueryException {

    public DataVolumeException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

}
