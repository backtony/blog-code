package com.springsecurity.authentication.domain;


import org.springframework.data.redis.core.RedisHash;

@RedisHash("logoutRefreshToken")
public class LogoutRefreshToken  extends Token {

    private LogoutRefreshToken(String id, long expiration) {
        super(id, expiration);
    }

    public static LogoutRefreshToken of (String accessToken, Long expiration){
        return new LogoutRefreshToken(accessToken,expiration);
    }
}
