package com.example.tomatomall.repository;

import com.example.tomatomall.po.CartItemPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemPO, Long> {
    List<CartItemPO> findByUserId(Long userId);
    Optional<CartItemPO> findByUserIdAndProductId(Long userId, Long productId);
    void deleteByUserIdAndCartItemId(Long userId, Long cartItemId);
} 