package com.example.tomatomall.vo;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderVO {
    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String status;
    private Timestamp createTime;
    private String receiverName;
    private String receiverPhone;
    private String receiverZipcode;
    private String receiverAddress;
    private String shippingAddress;
    private List<OrderItemVO> orderItems;
}
