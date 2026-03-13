package com.beyond.ordersystem.common.repostory;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.util.concurrent.FastThreadLocal.size;

//싱글톤
@Component
public class SseEmitterReqistory
{
//    SseEmitter 은 spring에서 제공하는 단방향 서버 연결 클레스
//    sseEmitter 객체는 사용자의 연결정보(ip,macadddress)를 의미
//    CON헤쉬맵은 스래드 세이프 맴
//    ConcurrentHasMAP<>() -> 양방향 맵 emitterMap의 키는 문자열(email)로 SseEmitter(값)
//    구조정의

    // SseEmitter 객체는 Java 메모리에서만 관리 가능
// - DB나 Redis에 직접 저장 불가 (직렬화 불가)
// - 멀티 서버 환경에서는 각 서버마다 emitterMap이 별도로 존재
//
// 메시지 데이터(JSON)는 Redis나 DB에 저장/전송 가능
// - Redis pub/sub을 통해 모든 서버로 메시지 전파 가능
// - 각 서버는 자신의 emitterMap에서 연결된 클라이언트에게 메시지 전달
//
// 결론:
// - emitterMap = 서버 단위 SSE 연결 관리용 Map
// - 메시지는 Redis, 객체는 메모리에만 존재 → 실시간 알림 구조
    private Map<String, SseEmitter> emitterMap =   new ConcurrentHashMap<>(); // emutterMap의 구조 정의
//    SseEmitter 등록 메서드 + addSseEmitter의 값 정의
//    map의 구조상 void
//    저장
    public void addSseEmitter(String email,SseEmitter ssemitter)
    {
        this.emitterMap.put(email,ssemitter);
    }
//    꺼내기 이메일로
    public SseEmitter getEmitter(String email)
    {
        return this.emitterMap.get(email);
    }
    public void removeEmitter(String email)
    {
        this.emitterMap.remove(email);
        System.out.println(size());
    }
}