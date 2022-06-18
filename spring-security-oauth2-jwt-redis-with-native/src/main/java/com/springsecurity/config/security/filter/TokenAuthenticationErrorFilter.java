package com.springsecurity.config.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springsecurity.common.exception.security.TokenAuthenticationFilterException;
import com.springsecurity.config.security.handler.SendErrorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAuthenticationErrorFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (RedisConnectionFailureException e){
            log.info("[TokenAuthenticationErrorFilter] {}",e.getMessage());
            SendErrorUtil.sendServerErrorResponse(response,objectMapper);
        }
        catch (TokenAuthenticationFilterException e) {
            log.info("[TokenAuthenticationErrorFilter] {}",e.getMessage());
            SendErrorUtil.sendInvalidTokenErrorResponse(response,objectMapper);
        }
    }
}
