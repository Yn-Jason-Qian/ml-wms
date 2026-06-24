package com.wms.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/** Spring Cache + Redis 配置 缓存高频读取的字典数据、SKU 列表等 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(30)) // 默认 TTL 30分钟
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new StringRedisSerializer()))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new GenericJackson2JsonRedisSerializer()))
                        .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                // 不同缓存区域不同 TTL
                .withCacheConfiguration(
                        "dict", defaultConfig.entryTtl(Duration.ofHours(2))) // 字典数据 2h
                .withCacheConfiguration(
                        "sku", defaultConfig.entryTtl(Duration.ofMinutes(10))) // SKU 列表 10min
                .withCacheConfiguration(
                        "warehouse", defaultConfig.entryTtl(Duration.ofHours(1))) // 仓库信息 1h
                .build();
    }
}
