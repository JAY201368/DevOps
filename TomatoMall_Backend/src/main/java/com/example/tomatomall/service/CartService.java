package com.example.tomatomall.service;

import com.example.tomatomall.vo.CartItemVO;
import com.example.tomatomall.vo.CartVO;
import com.example.tomatomall.vo.OrderVO;
import java.util.List;

public interface CartService {
    CartItemVO addToCart(Long userId, Long productId, Integer quantity);
    String removeFromCart(Long userId, Long cartItemId);
    String updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity);
    CartVO getCartItems(Long userId);
    OrderVO checkout(Long userId, List<String> cartItemIds, String receiverName, String receiverPhone, String receiverZipcode, String receiverAddress, String paymentMethod);
}