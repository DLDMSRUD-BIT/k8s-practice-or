package com.beyond.ordersystem.common.service;

import com.beyond.ordersystem.common.dtos.SseMessageDto;
import com.beyond.ordersystem.common.repostory.SseEmitterReqistory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

//알람
@Component
public class SseAlfrmService implements MessageListener {
    private final SseEmitterReqistory sseEmitterReqistory;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public SseAlfrmService(SseEmitterReqistory sseEmitterReqistory, ObjectMapper objectMapper,@Qualifier("ssePubSub") RedisTemplate<String, String> redisTemplate) {
        this.sseEmitterReqistory = sseEmitterReqistory;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public void sendMessage(String receiver,String sender, String message) {
//        SseEmitter sseEmitter = sseEmitterReqistory.getEmitter(receiver);
        SseMessageDto dto = SseMessageDto.builder()
                .receiver(receiver)
                .sender(sender)
                .message(message)
                .build();
//        직렬화
        try {
            String data = objectMapper.writeValueAsString(dto);
            SseEmitter sseEmitter = sseEmitterReqistory.getEmitter(dto.getReceiver());
//            해당 서버에 receiver의 emitter 객체가 있으면 send
            if(sseEmitter!=null){
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));
            }else {
//            레디스 PUB SUB의 기능을 활용하여 메시지 퍼블리쉬
                redisTemplate.convertAndSend("order-channel", data);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
//  massage : 실질적으로 메시지가 담겨있는 객체
//        patter:채널명
//        추후 여러개의 채널에 가기 메시지를 퍼리릭 하고
        String channelName = new String(pattern);
        System.out.println("messagebodt:"+ message.getBody());
        System.out.print("channelName:"+channelName);
//      해당 서버에 리시버에 리시버의 에미터 객체가 있으면 send
        try {
            SseMessageDto dto = objectMapper.readValue(message.getBody(), SseMessageDto.class);
            String data = objectMapper.writeValueAsString(dto);
            SseEmitter sseEmitter = sseEmitterReqistory.getEmitter(dto.getReceiver());
//            해당 서버에 receiver의 emitter 객체가 있으면 send
            if(sseEmitter!=null){
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));
            }

            System.out.println("messageBody : " + dto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
