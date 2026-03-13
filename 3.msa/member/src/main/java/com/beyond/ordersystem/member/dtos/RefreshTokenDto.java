package com.beyond.ordersystem.member.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RefreshTokenDto {
//    이게 POSTMAN이랑 동일한 명칭이여햐 함
    private  String refreshToken;
}
