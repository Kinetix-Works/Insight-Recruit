package com.insight_recruit.backend.app.auth;

import com.insight_recruit.backend.app.config.SecurityProperties;
import java.security.SecureRandom;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OtpStoreService {

    private final StringRedisTemplate redisTemplate;
    private final SecurityProperties securityProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpStoreService(StringRedisTemplate redisTemplate, SecurityProperties securityProperties) {
        this.redisTemplate = redisTemplate;
        this.securityProperties = securityProperties;
    }

    public void enforceRequestRateLimit(String identifier) {
        String key = otpRateKey(identifier);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            throw new IllegalStateException("Failed to enforce OTP rate limit");
        }
        if (count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(securityProperties.otpRateLimitWindowSeconds()));
        }
        if (count > securityProperties.otpRateLimitMax()) {
            throw new OtpRateLimitExceededException("Too many OTP requests. Try again later.");
        }
    }

    public String issueOtp(String identifier) {
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        redisTemplate.opsForValue().set(
            otpKey(identifier),
            otp,
            Duration.ofSeconds(securityProperties.otpTtlSeconds())
        );
        return otp;
    }

    public boolean verifyAndConsumeOtp(String identifier, String otp) {
        String key = otpKey(identifier);
        String expected = redisTemplate.opsForValue().get(key);
        if (expected == null || !expected.equals(otp)) {
            return false;
        }
        Boolean deleted = redisTemplate.delete(key);
        return Boolean.TRUE.equals(deleted);
    }

    private String otpKey(String identifier) {
        return "auth:otp:" + identifier;
    }

    private String otpRateKey(String identifier) {
        return "auth:otp:rate:" + identifier;
    }
}
