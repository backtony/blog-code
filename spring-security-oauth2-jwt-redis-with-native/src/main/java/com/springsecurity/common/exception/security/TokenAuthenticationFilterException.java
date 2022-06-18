package com.springsecurity.common.exception.security;

import org.springframework.http.HttpStatus;

public class TokenAuthenticationFilterException extends AuthException{

    private static final String MESSAGE = "유효하지 않은 토큰입니다.";
    private static final String CODE = "TOKEN-401";

    public TokenAuthenticationFilterException() {
        super(CODE, HttpStatus.UNAUTHORIZED, MESSAGE);
    }
}
