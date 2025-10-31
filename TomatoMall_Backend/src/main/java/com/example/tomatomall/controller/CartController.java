package com.example.tomatomall.controller;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.po.UserPO;
import com.example.tomatomall.repository.UserRepository;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.util.JwtUtil;
import com.example.tomatomall.vo.CartItemVO;
import com.example.tomatomall.vo.CartVO;
import com.example.tomatomall.vo.OrderVO;
import com.example.tomatomall.vo.ResultVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    // 获取当前登录用户
    private UserPO getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new TomatoMallException(401, "未授权");
        }
        
        String username = jwtUtil.getUsernameFromToken(token);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new TomatoMallException(404, "用户不存在"));
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping
    public ResultVO<CartItemVO> addToCart(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        try {
            UserPO currentUser = getCurrentUser(request);
            Long productId = Long.parseLong(requestBody.get("productId").toString());
            Integer quantity = Integer.parseInt(requestBody.get("quantity").toString());
            
            CartItemVO cartItemVO = cartService.addToCart(currentUser.getId(), productId, quantity);
            return ResultVO.buildSuccess(cartItemVO);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }

    /**
     * 从购物车删除商品
     */
    @DeleteMapping("/{cartItemId}")
    public ResultVO<String> removeFromCart(@PathVariable Long cartItemId, HttpServletRequest request) {
        try {
            UserPO currentUser = getCurrentUser(request);
            String result = cartService.removeFromCart(currentUser.getId(), cartItemId);
            return ResultVO.buildSuccess(result);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }

    /**
     * 修改购物车商品数量
     */
    @PatchMapping("/{cartItemId}")
    public ResultVO<String> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Integer> requestBody,
            HttpServletRequest request) {
        try {
            UserPO currentUser = getCurrentUser(request);
            Integer quantity = requestBody.get("quantity");
            
            String result = cartService.updateCartItemQuantity(currentUser.getId(), cartItemId, quantity);
            return ResultVO.buildSuccess(result);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }

    /**
     * 获取购物车商品列表
     */
    @GetMapping
    public ResultVO<CartVO> getCartItems(HttpServletRequest request) {
        try {
            UserPO currentUser = getCurrentUser(request);
            CartVO cartVO = cartService.getCartItems(currentUser.getId());
            return ResultVO.buildSuccess(cartVO);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }

    /**
     * 提交订单（结算购物车）
     */
    @PostMapping("/checkout")
    public ResultVO<OrderVO> checkout(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        try {
            UserPO currentUser = getCurrentUser(request);

            List<String> cartItemIds = (List<String>) requestBody.get("cartItemIds");
            Map<String, Object> shippingAddressObj = (Map<String, Object>) requestBody.get("shipping_address");
            String paymentMethod = requestBody.get("payment_method").toString();
            
            // 获取促销券ID（如果有）
            Long couponId = null;
            if (requestBody.containsKey("couponId") && requestBody.get("couponId") != null) {
                couponId = Long.parseLong(requestBody.get("couponId").toString());
            }

            String receiverName = shippingAddressObj.getOrDefault("name", "").toString();
            String receiverPhone = shippingAddressObj.getOrDefault("phone", "").toString();
            String receiverZipcode = shippingAddressObj.getOrDefault("zipcode", "").toString();
            String receiverAddress = shippingAddressObj.getOrDefault("address", "").toString();

            OrderVO orderVO = cartService.checkout(
                currentUser.getId(),
                cartItemIds,
                receiverName,
                receiverPhone,
                receiverZipcode,
                receiverAddress,
                paymentMethod,
                couponId
            );
            return ResultVO.buildSuccess(orderVO);
        } catch (TomatoMallException e) {
            return ResultVO.buildFailure(e.getMessage(), e.getCode().toString());
        } catch (Exception e) {
            return ResultVO.buildFailure("服务器内部错误", "500");
        }
    }
}