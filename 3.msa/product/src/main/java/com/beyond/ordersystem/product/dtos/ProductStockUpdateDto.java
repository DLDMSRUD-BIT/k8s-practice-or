package com.beyond.ordersystem.product.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductStockUpdateDto {
    private Long productId;
    private Integer productCount;
}
