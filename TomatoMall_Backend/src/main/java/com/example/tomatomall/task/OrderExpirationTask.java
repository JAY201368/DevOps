package com.example.tomatomall.task;

import com.example.tomatomall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderExpirationTask {

    @Autowired
    private OrderService orderService;

    // 每分钟检查一次超时订单
    @Scheduled(fixedRate = 60000)
    public void checkExpiredOrders() {
        orderService.handleExpiredOrders();
    }
}