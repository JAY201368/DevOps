package com.example.tomatomall.service;

import com.example.tomatomall.vo.OrderVO;
import com.example.tomatomall.vo.PaymentVO;
import java.util.List;

public interface OrderService {
    PaymentVO pay(Long orderId);

    // void handlePaymentCallback(String orderId, String alipayTradeNo, String amount, String tradeStatus);

    void updateOrderStatus(String orderId, String alipayTradeNo, String amount);

    void reduceStock(Long orderId);

    void handleExpiredOrders();

    List<OrderVO> getOrdersByUserId(Long userId);  // 添加获取订单列表的方法
    
    void cancelOrder(Long orderId, Long userId);  // 添加取消订单的方法
}
