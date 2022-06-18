package com.example.cleancode.exception;

public class HouseUtilsException extends RuntimeException{

    private ErrorCode errorCode;
    private String message;

    public HouseUtilsException(ErrorCode errorCode) {
        // 아래의 다른 생성자 호출
        this(errorCode, errorCode.getMessage());
    }

    public HouseUtilsException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.message = customMessage;
    }
}
