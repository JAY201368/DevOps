package com.example.tomatomall.service;

import com.example.tomatomall.vo.PaymentVO;

public interface OrderService {
    PaymentVO pay(Long orderId);

    void handlePaymentCallback(String orderId, String alipayTradeNo, String amount, String tradeStatus);

    void updateOrderStatus(String orderId, String alipayTradeNo, String amount);

    void reduceStock(Long orderId);

    void handleExpiredOrders();
}
