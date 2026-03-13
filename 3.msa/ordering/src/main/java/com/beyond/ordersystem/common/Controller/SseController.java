package com.beyond.ordersystem.common.Controller;

import com.beyond.ordersystem.common.repostory.SseEmitterReqistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/sse")
public class SseController {
    private final SseEmitterReqistory sseEmitterReqistory;

    @Autowired
    public SseController(SseEmitterReqistory sseEmitterReqistory) {
        this.sseEmitterReqistory = sseEmitterReqistory;
    }
//사용자 고유의 객채는 싱글톤으로 만들면 안된
    @GetMapping("/connect")
    public SseEmitter connect(@RequestHeader("X-User-Email")String email) throws IOException {
        System.out.println("connect");
        SseEmitter sseEmitter = new SseEmitter(60*60*1000L);//1시간 유효시간ㄴ
        sseEmitterReqistory.addSseEmitter(email,sseEmitter);

        sseEmitter.send(SseEmitter.event().name("connect").data("연결 완료"));
        return sseEmitter;
    }
    @GetMapping("/disconnect")
    public void disconnect(@RequestHeader("X-User-Email")String email) throws IOException {
        System.out.println("disconnect");
        sseEmitterReqistory.removeEmitter(email);
    }
}
