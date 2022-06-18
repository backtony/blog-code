package com.springsecurity.common.exception.security;

import com.springsecurity.common.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public abstract class AuthException extends ApplicationException {

    protected AuthException(String errorCode, HttpStatus httpStatus, String message) {
        super(errorCode, httpStatus, message);
    }
}
