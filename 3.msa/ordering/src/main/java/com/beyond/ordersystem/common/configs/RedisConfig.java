package com.beyond.ordersystem.common.configs;

import com.beyond.ordersystem.common.service.SseAlfrmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;



    @Configuration
    public class RedisConfig {
        @Value("${spring.redis.host}")
        private String host;
        @Value("${spring.redis.port}")
        private int port;

        @Bean
        @Qualifier("ssePubSub")
        public RedisConnectionFactory ssePubSubConnectionFactory(){
            RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
            configuration.setHostName(host);
            configuration.setPort(port);
//        configuration.setDatabase(1);
//        redis pub/sub 기능은 db에 값을 저장하는 기능이 아니므로, 특정 db에 의존적이지 않음.

            return new LettuceConnectionFactory(configuration);
        }

        //    템플릿 빈 객체
        @Bean
        @Qualifier("ssePubSub")
        public RedisTemplate<String, String> ssePubSubRedisTemplate(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory){
            RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(new StringRedisSerializer());
            redisTemplate.setConnectionFactory(redisConnectionFactory);
            return redisTemplate;
        }

        //    redis 리스너(subscribe) 객체
//    호출구조 : RedisMessageListenerContainer -> MessageListenerAdapter -> SseAlarmService(MessageListener)
        @Bean
        @Qualifier("ssePubSub")
        public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory, @Qualifier("ssePubSub") MessageListenerAdapter messageListenerAdapter){
            RedisMessageListenerContainer container = new RedisMessageListenerContainer();
            container.setConnectionFactory(redisConnectionFactory);
            container.addMessageListener(messageListenerAdapter, new PatternTopic("order-channel"));
//        만약 여러 채널을 구독해야 하는 경우, 여러개의 PatterTopic을 add하거나, 별도의 ListenerBean 객체 생성
            return container;
        }

        //    redis에서 수신된 메시지를 처리하는 객체
        @Bean
        @Qualifier("ssePubSub")
        public MessageListenerAdapter messageListenerAdapter(SseAlfrmService sseAlfrmService){
//        채널로부터 수신되는 message 처리를 SseAlarmService의 onMessage메서드로 위임
            return new MessageListenerAdapter(sseAlfrmService, "onMessage");
        }

    }





