package com.example.tomatomall.service;

import com.example.tomatomall.vo.CartItemVO;
import com.example.tomatomall.vo.CartVO;

public interface CartService {
    CartItemVO addToCart(Long userId, Long productId, Integer quantity);
    String removeFromCart(Long userId, Long cartItemId);
    String updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity);
    CartVO getCartItems(Long userId);
} 