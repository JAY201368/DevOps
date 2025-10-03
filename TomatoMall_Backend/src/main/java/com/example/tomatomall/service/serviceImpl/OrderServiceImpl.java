package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.OrderItemPO;
import com.example.tomatomall.po.OrderPO;
import com.example.tomatomall.repository.OrderRepository;
import com.example.tomatomall.service.OrderService;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.util.AliPay;
import com.example.tomatomall.util.AliPayController;
import com.example.tomatomall.vo.PaymentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AliPayController aliPayController;

    @Autowired
    private ProductService productService;

    @Override
    public PaymentVO pay(Long orderId) {
        // 1. 查询订单
        OrderPO order = orderRepository.findById(orderId)
                .orElseThrow(() -> new TomatoMallException(404, "订单不存在"));

        // 2. 检查订单状态
        if (!"PENDING".equals(order.getStatus())) {
            throw new TomatoMallException(400, "订单状态不正确");
        }

        try {
            // 3. 创建支付宝订单
            AliPay aliPay = new AliPay();
            aliPay.setTraceNo(orderId.toString());
            aliPay.setTotalAmount(order.getTotalAmount());
            aliPay.setSubject("TomatoMall订单-" + orderId);

            // 4. 生成支付表单
            String paymentForm = aliPayController.generatePayForm(aliPay);

            // 5. 构建返回结果
            PaymentVO paymentVO = new PaymentVO();
            paymentVO.setPaymentForm(paymentForm);
            paymentVO.setOrderId(orderId);
            paymentVO.setTotalAmount(order.getTotalAmount());
            paymentVO.setPaymentMethod("Alipay");

            return paymentVO;
        } catch (Exception e) {
            throw new TomatoMallException(500, "生成支付表单失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateOrderStatus(String orderId, String alipayTradeNo, String amount) {
        OrderPO order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new TomatoMallException(404, "订单不存在"));

        // 验证订单金额
        if (!order.getTotalAmount().toString().equals(amount)) {
            throw new TomatoMallException(400, "订单金额不匹配");
        }

        // 防止重复处理
        if ("SUCCESS".equals(order.getStatus())) {
            return;
        }

        // 更新订单状态
        order.setStatus("SUCCESS");
        orderRepository.save(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reduceStock(Long orderId) {
        OrderPO order = orderRepository.findById(orderId)
                .orElseThrow(() -> new TomatoMallException(404, "订单不存在"));

        // 只处理待支付订单
        if (!"PENDING".equals(order.getStatus())) {
            return;
        }

        // 获取订单中的商品并扣减库存
        for (OrderItemPO item : order.getOrderItems()) {
            boolean success = productService.reduceStock(item.getProductId(), item.getQuantity());
            if (!success) {
                throw new TomatoMallException(409, "库存更新失败，请重试");
            }
        }
    }

    @Override
    @Transactional
    public void handlePaymentCallback(String orderId, String alipayTradeNo, String amount, String tradeStatus) {
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            updateOrderStatus(orderId, alipayTradeNo, amount);
            reduceStock(Long.parseLong(orderId));
        }
    }
}
