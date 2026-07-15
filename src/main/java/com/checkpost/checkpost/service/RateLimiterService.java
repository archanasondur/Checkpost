package com.checkpost.checkpost.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;

@Service
public class RateLimiterService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public boolean exceedsLimit(Long agentId, Integer maxCallsPerMinute) {
        if (maxCallsPerMinute == null) return false;

        long currentMinute = Instant.now().getEpochSecond() / 60;
        String key = "ratelimit:" + agentId + ":" + currentMinute;

        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(90));
        }

        return count != null && count > maxCallsPerMinute;
    }
}