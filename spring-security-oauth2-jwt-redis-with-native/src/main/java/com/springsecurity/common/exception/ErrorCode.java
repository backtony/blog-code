package com.springsecurity.common.exception;

public enum ErrorCode {


    BINDING_EXCEPTION("FORM-400", "적절하지 않은 요청 값입니다."),
    MISSING_REQUEST_HEADER("HEADER-400", "특정 헤더값이 들어오지 않았습니다."),
    ;


    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public String getCode() {
        return code;
    }
}
