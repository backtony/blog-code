package com.springsecurity.authentication.application;

import com.springsecurity.config.security.dto.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class TokenProvider {

    public static final String TOKEN_TYPE = "Bearer ";

    private final String secretKey;
    private final long accessTokenExpirationTimeInMilliSeconds;
    private final long refreshTokenExpirationTimeInMilliSeconds;

    public TokenProvider(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.access-expiration-time}") long accessTokenExpirationTimeInMilliSeconds,
            @Value("${jwt.refresh-expiration-time}") long refreshTokenExpirationTimeInMilliSeconds) {
        this.secretKey = secretKey;
        this.accessTokenExpirationTimeInMilliSeconds = accessTokenExpirationTimeInMilliSeconds;
        this.refreshTokenExpirationTimeInMilliSeconds = refreshTokenExpirationTimeInMilliSeconds;
    }

    public String createAccessToken(Authentication authentication) {
        return createToken(authentication, accessTokenExpirationTimeInMilliSeconds);
    }

    public String createRefreshToken(Authentication authentication){
        return createToken(authentication, refreshTokenExpirationTimeInMilliSeconds);
    }

    private String createToken(Authentication authentication, long accessTokenExpirationTimeInMilliSeconds) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationTimeInMilliSeconds);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public String getUserEmailFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    public long getRemainingMilliSecondsFromToken(String token){
        Date expiration = getClaims(token).getExpiration();
        return expiration.getTime() - (new Date()).getTime();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }


    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
