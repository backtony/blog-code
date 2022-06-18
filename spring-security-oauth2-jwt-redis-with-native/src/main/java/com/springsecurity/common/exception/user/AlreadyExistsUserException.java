package com.springsecurity.common.exception.user;

import org.springframework.http.HttpStatus;

public class AlreadyExistsUserException extends UserException{

    private static final String MESSAGE = "중복되는 아이디가 존재합니다.";
    private static final String CODE = "SIGNUP-400";

    public AlreadyExistsUserException() {
        super(CODE, HttpStatus.BAD_REQUEST, MESSAGE);
    }
}
