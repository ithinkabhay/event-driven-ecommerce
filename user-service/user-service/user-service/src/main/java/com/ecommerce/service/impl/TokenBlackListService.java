package com.ecommerce.service.impl;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlackListService {

    private final RedisTemplate<String,String> redisTemplate;

    public TokenBlackListService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String BLACKLIST_PREFIX="blacklist:";

    public void blackListToken(String token, Long expirySecond){
        String key = BLACKLIST_PREFIX +  token;

        redisTemplate.opsForValue().set(key, "blacklisted", expirySecond, TimeUnit.SECONDS);
    }

    public boolean isTokenBlackList(String token){
        String key = BLACKLIST_PREFIX +  token;

        return redisTemplate.hasKey(key);
    }
}
