package com.beyond.ordersystem.member.controller;

import com.beyond.ordersystem.member.dtos.*;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.service.MemberService;
import com.beyond.ordersystem.common.auth.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MemberResDto> findAll(){
        List<MemberResDto> dtoList = memberService.findAll();
        return dtoList;
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findById(@PathVariable Long id){
        MemberResDto dto = memberService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @GetMapping("/myinfo")
    public ResponseEntity<?> myinfo(@AuthenticationPrincipal String email){
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
