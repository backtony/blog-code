package com.springsecurity.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String LOG_FORMAT = "Class : {}, Message : {}";
    private static final String LOG_CODE_FORMAT = "Class : {}, Code : {}, Message : {}";

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> bindException(BindException e) {

        final String code = ErrorCode.BINDING_EXCEPTION.getCode();
        final String message = ErrorCode.BINDING_EXCEPTION.getMessage();

        log.warn(LOG_CODE_FORMAT, e.getClass().getSimpleName(), code, message);

        ErrorResponse response = ErrorResponse.of(message, code, e.getBindingResult());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity handleMissingParams(MissingRequestHeaderException e) {

        String code = ErrorCode.MISSING_REQUEST_HEADER.getCode();
        String message = ErrorCode.MISSING_REQUEST_HEADER.getMessage();

        log.warn(LOG_CODE_FORMAT, e.getClass().getSimpleName(), code, message);

        ErrorResponse response = ErrorResponse.of(message,code);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> applicationException(ApplicationException e) {

        String errorCode = e.getErrorCode();
        String exceptionClassName = e.getClass().getSimpleName();
        String message = e.getMessage();
        ErrorResponse errorResponse = null;

        if (e.getErrors() != null) {
            log.warn(LOG_CODE_FORMAT, exceptionClassName, errorCode, "@valid");
            errorResponse = ErrorResponse.of(message, errorCode, e.getErrors());
        } else {
            log.warn(LOG_CODE_FORMAT, exceptionClassName, errorCode, message);
            errorResponse = ErrorResponse.of(message, errorCode);
        }

        return ResponseEntity
                .status(e.getHttpStatus())
                .body(errorResponse);
    }
}
