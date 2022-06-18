package com.springsecurity.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springsecurity.common.exception.ErrorResponse;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SendErrorUtil {

    private static final String INVALID_TOKEN_MESSAGE = "유효하지 않은 토큰입니다.";
    private static final String INVALID_TOKEN_CODE = "TOKEN-401";
    private static final String LOGIN_FAIL_MESSAGE = "로그인에 실패했습니다.";
    private static final String LOGIN_FAIL_CODE = "LOGIN-401";
    private static final String SERVER_ERROR_MESSAGE = "서버에 문제가 발생했습니다.";
    private static final String SERVER_ERROR_CODE = "SERVER-500";
    private static final String FORBIDDEN_MESSAGE = "권한이 없습니다.";
    private static final String FORBIDDEN_CODE = "LOGIN-403";


    public static void sendInvalidTokenErrorResponse(HttpServletResponse response, ObjectMapper objectMapper) throws IOException {
        String errorResponse = objectMapper.writeValueAsString(
                ErrorResponse.of(INVALID_TOKEN_MESSAGE, INVALID_TOKEN_CODE));
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        writeErrorResponse(response, errorResponse);
    }

    public static void sendLoginFailErrorResponse(HttpServletResponse response, ObjectMapper objectMapper) throws IOException {
        String errorResponse = objectMapper.writeValueAsString(
                ErrorResponse.of(LOGIN_FAIL_MESSAGE, LOGIN_FAIL_CODE));
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        writeErrorResponse(response, errorResponse);
    }

    public static void sendServerErrorResponse(HttpServletResponse response, ObjectMapper objectMapper) throws IOException {
        String errorResponse = objectMapper.writeValueAsString(
                ErrorResponse.of(SERVER_ERROR_MESSAGE, SERVER_ERROR_CODE));
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        writeErrorResponse(response, errorResponse);
    }

    public static void sendForbiddenErrorResponse(HttpServletResponse response, ObjectMapper objectMapper) throws IOException {
        String errorResponse = objectMapper.writeValueAsString(
                ErrorResponse.of(FORBIDDEN_MESSAGE, FORBIDDEN_CODE));
        response.setStatus(HttpStatus.FORBIDDEN.value());
        writeErrorResponse(response, errorResponse);
    }

    private static void writeErrorResponse(HttpServletResponse response, String errorResponse) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(errorResponse);
    }

}
