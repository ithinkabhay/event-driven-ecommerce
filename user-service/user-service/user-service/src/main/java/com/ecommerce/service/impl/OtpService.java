package com.ecommerce.service.impl;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;

    public OtpService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String OTP_PREFIX = "otp:";

    public void saveOtp(String phone, String otp) {

        String key = OTP_PREFIX + phone;
        redisTemplate.opsForValue().set(key, otp, 5, TimeUnit.MINUTES);
    }

    public boolean validateOtp(String phone, String otp) {
        String key = OTP_PREFIX + phone;
        String savedOtp = redisTemplate.opsForValue().get(key);

        if (savedOtp != null &&  savedOtp.equals(otp)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}
