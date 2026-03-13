package com.beyond.ordersystem.common.configs;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ResTemplateConfig {

//    유레카에 등록된 서비스명을 사용해서 내부서비스호출(내부통신)하는 어노테이션
    @LoadBalanced
    @Bean
    public RestTemplate makeRestTemplate() {
        return new RestTemplate();
    }
}
