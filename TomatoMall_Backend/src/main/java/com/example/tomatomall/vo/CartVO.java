package com.example.tomatomall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartVO {
    private List<CartItemVO> items;
    private Integer total;
    private BigDecimal totalAmount;
} 