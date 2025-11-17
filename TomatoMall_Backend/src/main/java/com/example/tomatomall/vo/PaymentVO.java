package com.example.tomatomall.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentVO {
    private String paymentForm;
    private Long orderId;
    private BigDecimal totalAmount;
    private String paymentMethod;
}
