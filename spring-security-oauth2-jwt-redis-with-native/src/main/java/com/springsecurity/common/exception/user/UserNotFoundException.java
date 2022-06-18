package com.springsecurity.common.exception.user;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends UserException{
    private static final String MESSAGE = "존재하지 않는 회원입니다.";
    private static final String CODE = "LOGIN-400";

    public UserNotFoundException() {
        super(CODE, HttpStatus.BAD_REQUEST, MESSAGE);
    }
}
