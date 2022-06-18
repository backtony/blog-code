package com.springsecurity.common.exception.security;

import org.springframework.http.HttpStatus;

public class NotSupportRegistrationIdException extends AuthException{
    private static final String MESSAGE = "지원하지 않는 소셜 서비스입니다.";
    private static final String CODE = "LOGIN-400";

    public NotSupportRegistrationIdException() {
        super(CODE, HttpStatus.BAD_REQUEST, MESSAGE);
    }
}

