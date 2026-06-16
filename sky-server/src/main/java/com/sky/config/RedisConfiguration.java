package com.sky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建RedisTemplate对象...");
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 设置 key 的序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // 设置 value 的序列化器，使用自定义的ObjectMapper支持LocalDateTime等类型
        ObjectMapper objectMapper = new JacksonObjectMapper();
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        redisTemplate.setValueSerializer(jsonRedisSerializer);
        // 设置 hash key 的序列化器
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        // 设置 hash value 的序列化器
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);
        return redisTemplate;
    }
}
