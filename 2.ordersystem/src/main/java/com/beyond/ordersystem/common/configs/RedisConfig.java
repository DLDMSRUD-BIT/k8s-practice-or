package com.beyond.ordersystem.common.configs;

import com.beyond.ordersystem.common.service.SseAlfrmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
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
    @Bean
//    같은 빈 객체가 여러개 있을 경우 빈 객체를 구분 하기 위 한 어노테이션
    @Qualifier ("stockInventory")
    public RedisConnectionFactory stockredisConnectionFactory()
    {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(1);
        return new LettuceConnectionFactory(configuration);
    }
    //    템플릿 빈 객체 필요
    @Bean
    @Qualifier ("stockInventory")
    public RedisTemplate<String, String> stockredisTemplate(@Qualifier("stockInventory") RedisConnectionFactory redisConnectionFactory)
    {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return  redisTemplate;
    }
//    4개의 bean인데 그중 같은 클레스 두개라 구분하기 위해 @Qualifier로 구분
@Bean
//    같은 빈 객체가 여러개 있을 경우 빈 객체를 구분 하기 위 한 어노테이션
@Qualifier ("ssePubSub")
public RedisConnectionFactory ssePubSubConnectionFactory()
{
    RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
    configuration.setHostName(host);
    configuration.setPort(port);
// db에 저장하는 값이 아니므로 특정db에 의존할 필요 없다
    return new LettuceConnectionFactory(configuration);
}
    //    템플릿 빈 객체 필요
    @Bean
    @Qualifier ("ssePubSub")
    public RedisTemplate<String, String> ssePubSubredisTemplate(@Qualifier("stockInventory") RedisConnectionFactory redisConnectionFactory)
    {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return  redisTemplate;
    }

//    호출 구조 : 레디스메세지리스너컨테이너에서 -> 메세지리스트어뎁터를 호출-> sse알람서비스(메세지리스너) 호출

//    레디스 리스너(subscribe) 객체
    @Bean
    @Qualifier("ssePubSub")
    public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("ssePubSub")MessageListenerAdapter messageListenerAdapter,@Qualifier("stockInventory") RedisConnectionFactory redisConnectionFactory){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter ,new PatternTopic("order-channel")); //이게 연결된 채널 이름 레디스에 넣을
//  여러채널을 구독해야할 경우 여러 패턴토픽을 add하거나 별도의 리스너 빈 객체 생성
        return  container;
    }


//    레디스에서 수신된 메세지를 처리하는 객체
//    채널부터 수신되는 메세지 처리를 ssealarmservice의 onmaessage메서드로 위임
    @Bean
    @Qualifier("ssePubSub")
    public MessageListenerAdapter messageListenerAdapter(SseAlfrmService sseAlfrmService){
        return new MessageListenerAdapter(sseAlfrmService,"onMessage");
    }




}
