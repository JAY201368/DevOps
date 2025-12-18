package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.controller.AliPayController;
import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.OrderItemPO;
import com.example.tomatomall.po.OrderPO;
import com.example.tomatomall.repository.OrderRepository;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.PaymentVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AliPayController aliPayController;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderPO testOrder;
    private OrderItemPO testOrderItem;

    @BeforeEach
    void setUp() {
        testOrder = new OrderPO();
        testOrder.setOrderId(1L);
        testOrder.setUserId(1L);
        testOrder.setTotalAmount(new BigDecimal("199.98"));
        testOrder.setOriginalAmount(new BigDecimal("199.98"));
        testOrder.setDiscountAmount(BigDecimal.ZERO);
        testOrder.setPaymentMethod("支付宝");
        testOrder.setStatus("PENDING");
        testOrder.setReceiverName("John Doe");
        testOrder.setReceiverPhone("13800138000");
        testOrder.setReceiverZipcode("100000");
        testOrder.setReceiverAddress("Test Address");
        testOrder.setCreateTime(new Timestamp(System.currentTimeMillis()));
        testOrder.setExpireTime(new Timestamp(System.currentTimeMillis() + 15 * 60 * 1000));

        testOrderItem = new OrderItemPO();
        testOrderItem.setId(1L);
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProductId(1L);
        testOrderItem.setQuantity(2);
        testOrderItem.setPrice(new BigDecimal("99.99"));
        testOrderItem.setProductTitle("Test Product");
        testOrderItem.setProductCover("test-cover.jpg");

        testOrder.setOrderItems(new ArrayList<>(Arrays.asList(testOrderItem)));
    }

    @Test
    void testPay_Success() throws Exception {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(aliPayController.generatePayForm(any())).thenReturn("<form>payment form</form>");

        PaymentVO result = orderService.pay(1L);

        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
        assertEquals("Alipay", result.getPaymentMethod());
        assertEquals(new BigDecimal("199.98"), result.getTotalAmount());
        assertNotNull(result.getPaymentForm());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testPay_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            orderService.pay(999L);
        });
        assertEquals(404, exception.getCode());
    }

    @Test
    void testPay_OrderNotPending() {
        testOrder.setStatus("SUCCESS");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            orderService.pay(1L);
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testPay_PaymentFormGenerationFails() throws Exception {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(aliPayController.generatePayForm(any())).thenThrow(new RuntimeException("Payment failed"));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            orderService.pay(1L);
        });
        assertEquals(500, exception.getCode());
    }

    @Test
    void testUpdateOrderStatus_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(OrderPO.class))).thenReturn(testOrder);

        orderService.updateOrderStatus("1", "alipay-trade-123", "199.98");

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(OrderPO.class));
    }

    @Test
    void testUpdateOrderStatus_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            orderService.updateOrderStatus("999", "alipay-trade-123", "199.98");
        });
        assertEquals(404, exception.getCode());
    }

    @Test
    void testUpdateOrderStatus_AmountMismatch() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            orderService.updateOrderStatus("1", "alipay-trade-123", "299.98");
        });
        assertEquals(400, exception.getCode());
    }

    @Test
    void testUpdateOrderStatus_AlreadySuccess() {
        testOrder.setStatus("SUCCESS");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        orderService.updateOrderStatus("1", "alipay-trade-123", "199.98");

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(0)).save(any(OrderPO.class));
    }

    @Test
    void testReduceStock_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(productService.reduceStock(1L, 2)).thenReturn(true);

        orderService.reduceStock(1L);

        verify(productService, times(1)).reduceStock(1L, 2);
    }

    @Test
    void testReduceStock_OrderNotPending() {
        testOrder.setStatus("SUCCESS");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        orderService.reduceStock(1L);

        verify(productService, times(0)).reduceStock(anyLong(), anyInt());
    }

    @Test
    void testReduceStock_StockUpdateFails() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(productService.reduceStock(1L, 2)).thenReturn(false);

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            orderService.reduceStock(1L);
        });
        assertEquals(409, exception.getCode());
    }

    @Test
    void testReduceStock_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            orderService.reduceStock(999L);
        });
        assertEquals(404, exception.getCode());
    }

    @Test
    void testHandlePaymentCallback_TradeSuccess() {
        // Order needs to be in PENDING status initially
        testOrder.setStatus("PENDING");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(OrderPO.class))).thenAnswer(invocation -> {
            OrderPO order = invocation.getArgument(0);
            order.setStatus("SUCCESS");
            return order;
        });

        orderService.handlePaymentCallback("1", "alipay-trade-123", "199.98", "TRADE_SUCCESS");

        // The order status should be updated to SUCCESS
        verify(orderRepository, times(1)).save(any(OrderPO.class));
        // reduceStock is called but doesn't actually reduce stock because order status is already SUCCESS after updateOrderStatus
        // This is the actual behavior - stock is reduced during checkout, not during payment callback
    }

    @Test
    void testHandlePaymentCallback_TradeNotSuccess() {
        orderService.handlePaymentCallback("1", "alipay-trade-123", "199.98", "TRADE_FAILED");

        verify(orderRepository, times(0)).findById(anyLong());
        verify(orderRepository, times(0)).save(any(OrderPO.class));
    }

    @Test
    void testHandleExpiredOrders() {
        OrderPO expiredOrder = new OrderPO();
        expiredOrder.setOrderId(2L);
        expiredOrder.setStatus("PENDING");
        expiredOrder.setExpireTime(new Timestamp(System.currentTimeMillis() - 1000));

        OrderItemPO expiredItem = new OrderItemPO();
        expiredItem.setProductId(1L);
        expiredItem.setQuantity(1);
        expiredOrder.setOrderItems(new ArrayList<>(Arrays.asList(expiredItem)));

        when(orderRepository.findExpiredOrders(any(Timestamp.class))).thenReturn(Arrays.asList(expiredOrder));
        when(productService.restoreStock(1L, 1)).thenReturn(true);
        when(orderRepository.save(any(OrderPO.class))).thenReturn(expiredOrder);

        orderService.handleExpiredOrders();

        verify(productService, times(1)).restoreStock(1L, 1);
        verify(orderRepository, times(1)).save(any(OrderPO.class));
    }

    @Test
    void testGetOrdersByUserId() {
        when(orderRepository.findByUserId(1L)).thenReturn(Arrays.asList(testOrder));

        var result = orderService.getOrdersByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getOrderId());
        assertEquals(1, result.get(0).getOrderItems().size());
        verify(orderRepository, times(1)).findByUserId(1L);
    }

    @Test
    void testGetOrdersByUserId_EmptyList() {
        when(orderRepository.findByUserId(1L)).thenReturn(Arrays.asList());

        var result = orderService.getOrdersByUserId(1L);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testCancelOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(OrderPO.class))).thenReturn(testOrder);
        when(productService.restoreStock(1L, 2)).thenReturn(true);

        orderService.cancelOrder(1L, 1L);

        verify(orderRepository, times(1)).save(any(OrderPO.class));
        verify(productService, times(1)).restoreStock(1L, 2);
    }

    @Test
    void testCancelOrder_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            orderService.cancelOrder(999L, 1L);
        });
        assertEquals(404, exception.getCode());
    }

    @Test
    void testCancelOrder_UnauthorizedUser() {
        testOrder.setUserId(2L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            orderService.cancelOrder(1L, 1L);
        });
        assertEquals(403, exception.getCode());
    }

    @Test
    void testCancelOrder_OrderNotPending() {
        testOrder.setStatus("SUCCESS");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        TomatoMallException exception = assertThrows(TomatoMallException.class, () -> {
            orderService.cancelOrder(1L, 1L);
        });
        assertEquals(400, exception.getCode());
    }
}
