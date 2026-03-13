package com.beyond.ordersystem.product.domain;

import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import com.beyond.ordersystem.product.dtos.ProductUpdateDto;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@Entity
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;
    private String imagePath;
    private String memberemail;

    public void updateProfileImageUrl(String url){
        this.imagePath = url;
    }

    public void updateStockQuantity(int orderQuantity){
        this.stockQuantity = this.stockQuantity - orderQuantity;
    }

    public void updateProduct(ProductUpdateDto dto)
    {
        this.name = dto.getName();
        this.category = dto.getCategory();
        this.stockQuantity = dto.getStockQuantity();
        this.price = dto.getPrice();
    }

}
