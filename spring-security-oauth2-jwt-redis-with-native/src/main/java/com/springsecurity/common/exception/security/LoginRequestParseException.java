package com.springsecurity.common.exception.security;

import org.springframework.http.HttpStatus;

public class LoginRequestParseException extends AuthException{

    private static final String MESSAGE = "LoginRequest 파싱에 실패했습니다.";
    private static final String CODE = "LOGIN-400";

    public LoginRequestParseException() {
        super(CODE, HttpStatus.BAD_REQUEST, MESSAGE);
    }
}
