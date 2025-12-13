package com.ecommerce.service.impl;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginRateLimitService {

    private static final int MAX_ATTEMPTS = 5;

    private static final int BLOCK_TIME_MINUTES = 10;

    private static final String PREFIX = "LOGIN_FAIL:";

    private final RedisTemplate<String, String> redisTemplate;

    public LoginRateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isBlocked(String username){
        String key = PREFIX + username;
        String attempts = redisTemplate.opsForValue().get(key);

        return  attempts != null && Integer.parseInt(attempts) > MAX_ATTEMPTS;
    }

    public void onFailure(String username){
        String key = PREFIX + username;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1){
            redisTemplate.expire(key, BLOCK_TIME_MINUTES, TimeUnit.MINUTES);
        }
    }

    public void onSuccess(String username){
        redisTemplate.delete(PREFIX + username);
    }

}
