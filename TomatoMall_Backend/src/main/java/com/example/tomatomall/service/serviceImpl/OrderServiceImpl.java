package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.controller.AliPayController;
import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.OrderItemPO;
import com.example.tomatomall.po.OrderPO;
import com.example.tomatomall.repository.OrderRepository;
import com.example.tomatomall.service.OrderService;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.util.AliPay;
import com.example.tomatomall.vo.OrderVO;
import com.example.tomatomall.vo.OrderItemVO;
import com.example.tomatomall.vo.PaymentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

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

            // 5. 构建返回结果 - 使用统一的支付方式标识
            PaymentVO paymentVO = new PaymentVO();
            paymentVO.setPaymentForm(paymentForm);
            paymentVO.setOrderId(orderId);
            paymentVO.setTotalAmount(order.getTotalAmount());
            paymentVO.setPaymentMethod("Alipay");  // 统一使用英文标识

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
    //无用的回调函数，因为库存已经在下单时扣减了
    // @Override
    // @Transactional
    // public void handlePaymentCallback(String orderId, String alipayTradeNo, String amount, String tradeStatus) {
    //     if ("TRADE_SUCCESS".equals(tradeStatus)) {
    //         updateOrderStatus(orderId, alipayTradeNo, amount);
    //         reduceStock(Long.parseLong(orderId));
    //     }
    // }

    @Override
    @Transactional
    public void handleExpiredOrders() {
        // 获取所有已超时的待支付订单
        List<OrderPO> expiredOrders = orderRepository.findExpiredOrders(new Timestamp(System.currentTimeMillis()));

        for (OrderPO order : expiredOrders) {
            // 恢复库存
            for (OrderItemPO item : order.getOrderItems()) {
                productService.restoreStock(item.getProductId(), item.getQuantity());
            }
            // 更新订单状态为已取消
            order.setStatus("CANCELLED");
            orderRepository.save(order);
        }
    }

    @Override
    public List<OrderVO> getOrdersByUserId(Long userId) {
        List<OrderPO> orders = orderRepository.findByUserId(userId);
        
        // 转换订单列表为VO
        return orders.stream().map(order -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            
            // 转换订单项
            List<OrderItemVO> orderItemVOs = order.getOrderItems().stream().map(item -> {
                OrderItemVO itemVO = new OrderItemVO();
                BeanUtils.copyProperties(item, itemVO);
                // 计算小计金额
                itemVO.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                return itemVO;
            }).collect(Collectors.toList());
            
            orderVO.setOrderItems(orderItemVOs);
            return orderVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        OrderPO order = orderRepository.findById(orderId)
                .orElseThrow(() -> new TomatoMallException(404, "订单不存在"));
                
        // 验证订单所属
        if (!order.getUserId().equals(userId)) {
            throw new TomatoMallException(403, "无权操作此订单");
        }
        
        // 验证订单状态
        if (!"PENDING".equals(order.getStatus())) {
            throw new TomatoMallException(400, "只能取消待支付的订单");
        }
        
        // 更新订单状态为已取消
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        
        // 恢复库存
        for (OrderItemPO item : order.getOrderItems()) {
            productService.restoreStock(item.getProductId(), item.getQuantity());
        }
    }
}
