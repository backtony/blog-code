package com.springsecurity.config.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springsecurity.common.exception.security.HttpMethodNotSupportException;
import com.springsecurity.common.exception.security.LoginRequestParseException;
import com.springsecurity.config.security.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        if (!request.getMethod().equals(HttpMethod.POST.name())) {
            throw new HttpMethodNotSupportException();
        }

        LoginRequest loginRequest;
        try {
            loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
        } catch (IOException e) {
            throw new LoginRequestParseException();
        }

        String email = loginRequest.getEmail(); // spring security에서는 username이 라는것 기억
        String password = loginRequest.getPassword();

        if (!StringUtils.hasText(email)) {
            email = "";
        }

        if (!StringUtils.hasText(password)) {
            password = "";
        }

        email = email.trim();

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                email, password);

        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }
}
