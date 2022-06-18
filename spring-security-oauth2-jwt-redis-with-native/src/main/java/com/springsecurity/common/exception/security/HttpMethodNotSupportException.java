package com.springsecurity.common.exception.security;

import org.springframework.http.HttpStatus;

public class HttpMethodNotSupportException extends AuthException{

    private static final String MESSAGE = "지원하지 않는 HTTP METHOD 입니다.";
    private static final String CODE = "LOGIN-400";

    public HttpMethodNotSupportException() {
        super(CODE, HttpStatus.BAD_REQUEST, MESSAGE);
    }
}

