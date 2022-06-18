package com.example.springehcache.config;

import lombok.Getter;

@Getter
public enum CacheKey {
    MEMBER("member",60)
    ;

    private final String key;
    private final int exp;

    CacheKey(String key, int exp) {
        this.key = key;
        this.exp = exp;
    }
}
