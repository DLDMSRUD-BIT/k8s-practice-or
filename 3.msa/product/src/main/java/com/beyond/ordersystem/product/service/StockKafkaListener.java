package com.beyond.ordersystem.product.service;

import com.beyond.ordersystem.product.dtos.ProductStockUpdateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

@Configuration
public class StockKafkaListener {

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    public StockKafkaListener(ProductService productService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "stock-update-topic", containerFactory = "kafkaListener")
    public void stockConsumer(String message) throws JsonProcessingException {
        ProductStockUpdateDto dto = objectMapper.readValue(message, ProductStockUpdateDto.class);
        productService.updateStock(dto);
    }
}
