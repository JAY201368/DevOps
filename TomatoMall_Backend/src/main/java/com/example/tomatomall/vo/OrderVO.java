package com.example.tomatomall.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long orderId;
    private Long userId;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String status;
    private LocalDateTime createTime;
    private String receiverName;
    private String receiverPhone;
    private String receiverZipcode;
    private String receiverAddress;
    private String shippingAddress;
    private Long couponId;
    private String alipayTradeNo;
    private LocalDateTime updateTime;
    private LocalDateTime expireTime;
    private List<OrderItemVO> orderItems;
}
