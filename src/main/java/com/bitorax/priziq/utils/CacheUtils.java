package com.bitorax.priziq.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CacheUtils {

    RedisTemplate<String, Object> redisTemplate;
    ObjectMapper objectMapper;

    public <T> void cacheHash(String key, T object, long ttlSeconds) {
        try {
            Map<String, Object> map = objectMapper.convertValue(object, Map.class);
            redisTemplate.opsForHash().putAll(key, map);
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to cache hash: " + key, e);
        }
    }

    public <T> T getCachedHash(String key, Class<T> clazz) {
        try {
            Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
            if (map.isEmpty()) return null;
            return objectMapper.convertValue(map, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get cached hash: " + key, e);
        }
    }

    public <T> void cacheList(String key, List<T> list, long ttlSeconds) {
        try {
            redisTemplate.delete(key);
            for (T item : list) {
                redisTemplate.opsForList().rightPush(key, item);
            }
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to cache list: " + key, e);
        }
    }

    public <T> List<T> getCachedList(String key, Class<T> clazz) {
        try {
            List<Object> rawList = redisTemplate.opsForList().range(key, 0, -1);
            if (rawList == null || rawList.isEmpty()) return List.of();
            return rawList.stream()
                    .map(item -> objectMapper.convertValue(item, clazz))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get cached list: " + key, e);
        }
    }

    public void deleteCache(String... keys) {
        try {
            redisTemplate.delete(List.of(keys));
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete cache: " + String.join(", ", keys), e);
        }
    }
}