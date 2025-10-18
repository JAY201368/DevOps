package com.example.tomatomall.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemVO {
    private Long id;
    private Integer orderId;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
    private String productTitle;
    private String productCover;
    private BigDecimal subtotal;  // 小计金额（价格 * 数量）
}
