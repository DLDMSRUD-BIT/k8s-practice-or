package com.beyond.ordersystem.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//알람
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SseMessageDto {
    private String receiver;
    private String sender;
    private String message;
}
