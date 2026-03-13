package com.beyond.ordersystem.member.controller;

import com.beyond.ordersystem.member.dtos.*;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.service.MemberService;
import com.beyond.ordersystem.common.auth.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    @Autowired
    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody MemberCreateDto dto){
        Long id = memberService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }


    @GetMapping("/detail/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        MemberResDto dto = memberService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @GetMapping("/myinfo")
//    x로 시작하는 헤더 명은 개발자가 임의적으로 만든 header인경우에 사용
    public ResponseEntity<?> myinfo(@RequestHeader("X-User-Email")String email){
        MemberResDto dto = memberService.myinfo(email);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }


    @PostMapping("/doLogin")
    public ResponseEntity<?> login(@RequestBody MemberLoginReqDto dto) {
        Member member = memberService.login(dto);
        String accessToken = jwtTokenProvider.createToken(member);
//        refresh생성 및 저장
        String refreshToken = jwtTokenProvider.creatertToken(member);
        MemberLoginResDto memberLoginResDto = MemberLoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(memberLoginResDto);
    }
//    @Transactional(readOnly = true)
//    @GetMapping("/list")
//    public ResponseEntity<?> findAll(){
//        List<MemberListResDto> memberListResDto = new ArrayList<>();
//        memberListResDto = memberService.findAll();
//        return ResponseEntity.status(HttpStatus.OK).body(memberListResDto);
//    }
//    정상인 경우 at 매개변수로 rt를 받음
//    바디로 나와야되서 dto생성
    @PostMapping("/refresh-at")
    public ResponseEntity<?> refreshAt(@RequestBody RefreshTokenDto dto){
//        rt검증(1.토큰 자체 검증->파싱 2.redis 조회 검증)
        Member member = jwtTokenProvider.validataRt(dto.getRefreshToken());
//        at를 신규 생성해서 return
        String accessToken = jwtTokenProvider.createToken(member);
//        refresh생성 및 저장
        String refreshToken = jwtTokenProvider.creatertToken(member);
        MemberLoginResDto memberLoginResDto = MemberLoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(null)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(memberLoginResDto);

    }

}
