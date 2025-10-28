package com.example.tomatomall.controller;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.example.tomatomall.util.AliPay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AliPayController {
    @Value("${alipay.app-id}")
    private String appId;
    
    @Value("${alipay.private-key}")
    private String privateKey;
    
    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;
    
    @Value("${alipay.server-url}")
    private String serverUrl;
    
    @Value("${alipay.charset}")
    private String charset;
    
    @Value("${alipay.sign-type}")
    private String signType;
    
    @Value("${alipay.notify-url}")
    private String notifyUrl;
    
    @Value("${alipay.return-url}")
    private String returnUrl;

    private static final String FORMAT = "JSON";

    public String generatePayForm(AliPay aliPay) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl, appId,
                privateKey, FORMAT, charset, alipayPublicKey, signType);

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);
        
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", aliPay.getTraceNo());
        bizContent.put("total_amount", aliPay.getTotalAmount());
        bizContent.put("subject", aliPay.getSubject());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        
        request.setBizContent(bizContent.toString());
        
        return alipayClient.pageExecute(request).getBody();
    }
}
