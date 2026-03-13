package com.beyond.ordersystem.common.configs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig
{
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
//    연결 빈 객체 필요
    @Bean
//    같은 빈 객체가 여러개 있을 경우 빈 객체를 구분 하기 위 한 어노테이션
    @Qualifier ("rtInventory")
    public RedisConnectionFactory redisConnectionFactory()
    {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0);
        return new LettuceConnectionFactory(configuration);
    }
//    템플릿 빈 객체 필요
    @Bean
    @Qualifier("rtInventory")
//    모든 템플리중에 무조건 redistemplate이라는 메서드명이 반드시 1개는 존재해야됨
//    빈 템플릿 전체 중에 꼭 1개는 있어야함
//    빈 객체 생성시, 빈객체간에 di(의존성주입)는 메서드 파라미터 주입이 가능
//    시리어라이즈 스트링으로 변화해주는 직렬화
    public RedisTemplate<String, String> redisTemplate(@Qualifier("rtInventory") RedisConnectionFactory redisConnectionFactory)
    {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return  redisTemplate;
    }








}
