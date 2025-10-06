package com.example.tomatomall.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.example.tomatomall.service.OrderService;
import com.example.tomatomall.vo.PaymentVO;
import com.example.tomatomall.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OrdersController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/{orderId}/pay")
    public ResultVO<PaymentVO> pay(@PathVariable Long orderId) {
        try {
            PaymentVO paymentVO = orderService.pay(orderId);
            return ResultVO.buildSuccess(paymentVO);
        } catch (Exception e) {
            return ResultVO.buildFailure(e.getMessage(), "500");
        }
    }

    @PostMapping("/notify")
    public void handleAlipayNotify(HttpServletRequest request, HttpServletResponse response) throws IOException{
        // 1. 解析支付宝回调参数（通常是 application/x-www-form-urlencoded）
        Map<String, String> params = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));

        // 2. 验证支付宝签名（防止伪造请求）
        boolean signVerified;
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAre7Oxxb0V3TLI8vKm2/tPIXOb2Lrve74THGKu2SRHdoKZlePdtOlZy9hwDx/bcItxOTE42viNDqlcR4erDABB+l3HwHUgZQ+Zngik/T5uYe5bkQdJLE2iXDydGKOEA0lYPbwT1h35wFfbRQIxpeyIRPmA0W6vgNu6Kp8ZFllrLeeQNBakKhhSEar/u626N2lMuZzmS/5x4Ljn+4NJif+3KULSTxiuCeVly4XI/vu/poSq9bLLaX9teWMH04NTYxNmmPyUWsYK/1gI/6O8wRcPdFFPrRpu2Kpz0RZbwICMDLuMYUFQMLkqJzYGIzdBtb+69An+BP7xnog1e8ncBrcMQIDAQAB", "UTF-8", "RSA2");
        } catch (Exception e) {
            response.getWriter().print("fail"); // 签名验证失败，返回 fail
            return;
        }
        if (!signVerified) {
            response.getWriter().print("fail"); // 签名验证失败，返回 fail
            return;
        }

        // 3. 处理业务逻辑（更新订单、减库存等）
        String tradeStatus = params.get("trade_status");
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            String orderId = params.get("out_trade_no"); // 您的订单号
            String alipayTradeNo = params.get("trade_no"); // 支付宝交易号
            String amount = params.get("total_amount"); // 支付金额

            // 更新订单状态（注意幂等性，防止重复处理）
            orderService.updateOrderStatus(orderId, alipayTradeNo, amount);

            // 扣减库存（建议加锁或乐观锁）
            
        }

        // 4. 必须返回纯文本的 "success"（支付宝要求）
        response.getWriter().print("success");
    }
}
