package com.beyond.ordersystem;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HeathCheckController {
    @GetMapping("/health")
    public String healthCheck(){
        return "ok333";
    }
}
