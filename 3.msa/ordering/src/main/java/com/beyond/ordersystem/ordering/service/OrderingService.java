package com.beyond.ordersystem.ordering.service;

import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dtos.OrderCreateDto;
import com.beyond.ordersystem.ordering.dtos.OrderListDto;
import com.beyond.ordersystem.ordering.dtos.ProductDto;
import com.beyond.ordersystem.ordering.feigncliets.ProductFeignClient;
import com.beyond.ordersystem.ordering.repository.OrderDetailRepository;
import com.beyond.ordersystem.ordering.repository.OrderingRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final RestTemplate restTemplate;
    private final ProductFeignClient productFeignClient;
    private final KafkaTemplate kafkaTemplate;

    public OrderingService(OrderingRepository orderingRepository, OrderDetailRepository orderDetailRepository, @Qualifier("ssePubSub") RedisTemplate<String, String> redisTemplate, RestTemplate restTemplate, ProductFeignClient productFeignClient, KafkaTemplate kafkaTemplate) {
        this.orderingRepository = orderingRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.restTemplate = restTemplate;
        this.productFeignClient = productFeignClient;
        this.kafkaTemplate = kafkaTemplate;
    }
    public Long create( List<OrderCreateDto> orderCreateDtoList,String email){
        Ordering ordering = Ordering.builder()
                .memberEmail(email)
                .build();
//        부모엔티티 캐스캐이딩을 한다고 하더라도 save를 먼저 하고 해라
        orderingRepository.save(ordering);
//        1.재고 조회(http요청)
        for (OrderCreateDto dto : orderCreateDtoList){
//              유레카 한테 물어보는 코드(restempateconfig의 로드밸런스 어노테이션이)
//            http://localhost:8080/product-service : apigateway을 통한 호출
//              httl://유레카한테 질의 후 프로덕트 서비스에게 직접 호출
            String endpoint1 = "http://product-service/product/detail/"+dto.getProductId();
            HttpHeaders headers = new HttpHeaders();
//            httpentity는 header+boby :바디 필요없어서 뺌 get요청이라
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<ProductDto> responseEntity= restTemplate.exchange(endpoint1, HttpMethod.GET,httpEntity,ProductDto.class);
            ProductDto product = responseEntity.getBody();
            if(product.getStockQuantity()<dto.getProductCount()) {
                throw new IllegalArgumentException(("재고가 부족합니다"));
            }
//            2.주문 발생
            OrderDetail orderDetail = OrderDetail.builder()
                    .ordering(ordering)
                    .productName(product.getName())
                    .productId(dto.getProductId())
                    .quantity(dto.getProductCount())
                    .build();
            orderDetailRepository.save(orderDetail);
            String endpoint2 = "http://product-service/product/updatestock";
            HttpHeaders headers2 = new HttpHeaders();
            headers2.setContentType(MediaType.APPLICATION_JSON);
//            httpentity는 header+boby :바디 필요없어서 뺌 get요청이라
            HttpEntity<OrderCreateDto> httpEntity2 = new HttpEntity<>(dto,headers2);
//            System.out.println(httpEntity2);
            restTemplate.exchange(endpoint2, HttpMethod.PUT, httpEntity2, Void.class);

        }
        return ordering.getId();
    }
    public Long createfeign( List<OrderCreateDto> orderCreateDtoList,String email){
        Ordering ordering = Ordering.builder()
                .memberEmail(email)
                .build();
        orderingRepository.save(ordering);
//        1.재고 조회(http요청)
        for (OrderCreateDto dto : orderCreateDtoList){
            ProductDto product = productFeignClient.getProductById(dto.getProductId());
            if(product.getStockQuantity()<dto.getProductCount()) {
                throw new IllegalArgumentException(("재고가 부족합니다"));
            }
            OrderDetail orderDetail = OrderDetail.builder()
                    .ordering(ordering)
                    .productName(product.getName())
                    .productId(dto.getProductId())
                    .quantity(dto.getProductCount())
                    .build();
            orderDetailRepository.save(orderDetail);
//            fegin을 사용한 동기적 재고 감소 요청
//            productFeignClient.updateStockQuantity(dto);
//            카프카를 활용한 비동기적 재고감소 요청
            kafkaTemplate.send("stock-update-topic", dto);

        }
        return ordering.getId();
    }


    @Transactional(readOnly = true)
    public List<OrderListDto> findAll(){
        return orderingRepository.findAll().stream().map(o->OrderListDto.fromEntity(o)).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<OrderListDto> myorders(String email){
        return orderingRepository.findAllByMemberEmail(email).stream().map(o->OrderListDto.fromEntity(o)).collect(Collectors.toList());
    }

}
