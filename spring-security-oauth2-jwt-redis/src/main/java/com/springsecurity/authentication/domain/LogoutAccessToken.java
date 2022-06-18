package com.springsecurity.authentication.domain;


import org.springframework.data.redis.core.RedisHash;

@RedisHash("logoutAccessToken")
public class LogoutAccessToken extends Token {

    private LogoutAccessToken(String id, long expiration) {
        super(id, expiration);
    }

    public static LogoutAccessToken of (String accessToken, long expiration){
        return new LogoutAccessToken(accessToken,expiration);
    }
}
