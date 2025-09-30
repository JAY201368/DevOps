package com.example.tomatomall.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemVO {
    private Long id;
    private Long productId;
    private String productTitle;
    private String productCover;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}
