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
    //sse연결이 있을 경우 직접 전송 없을 경우 redus Pub.sub
//    하나의 서버일땐 sse로 끝
    public void sendMessage(String receiver,String sender, String message) {
//        SseEmitter sseEmitter = sseEmitterReqistory.getEmitter(receiver);
        SseMessageDto dto = SseMessageDto.builder()
                .receiver(receiver)
                .sender(sender)
                .message(message)
                .build();
//        직렬화(json문자열로 변경)
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
    //    두개의 서버이리때 구독해놓은 redis가 전파한 메세지를 객체로 만들어 리시브해서 ssemmttier을 생성해 다시 json으로 직렬화 후 출력
//    멀티 서버 환경일 경우만 필요 위데 implements MessageListener 먼저 생성
//    위에 메소드에서 이미 값을 받아서 java객체로 재직렬화(그래야 값을 확인) -> 출력을 위한 정렬화
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
//            dto 역직렬화
            SseMessageDto dto = objectMapper.readValue(message.getBody(), SseMessageDto.class);
            String data = objectMapper.writeValueAsString(dto);
            SseEmitter sseEmitter = sseEmitterReqistory.getEmitter(dto.getReceiver());
//            해당 서버에 receiver의 emitter 객체가 있으면 send
            if(sseEmitter!=null){
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));
            }

            System.out.println("messageBody : " + dto);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
//    즉 서버와 클라이언트가 연결되어 sseemitter이 꼭 필요한데 전송에 이게 같은 서버일경우에는 sse로 보내고 끝 이걸 판단하기 윟0
//    sseemitter의 존재 여부 보내는 곳의 있으면 그냥 sse로 보내고 없으면 redis로 전송 할거 근데 이미 json으로 받았어 근데 sseemitter이
//    존재해야되기 때문에 자바 객체로 바꿔주고 sseemitter을 생성후 다시 출력을 위해 json으로 버꾸는 과정
}

