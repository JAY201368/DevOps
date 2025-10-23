package com.example.tomatomall.repository;

import com.example.tomatomall.po.CommentPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<CommentPO, Long> {
    // 获取商品的评论列表（分页）
    Page<CommentPO> findByProductIdAndStatus(Long productId, Integer status, Pageable pageable);
    
    // 检查用户是否已评论过该商品
    boolean existsByUserIdAndProductIdAndStatus(Long userId, Long productId, Integer status);
    
    // 获取用户对商品的评论
    CommentPO findByUserIdAndProductIdAndStatus(Long userId, Long productId, Integer status);
    
    // 检查用户是否购买过商品（通过订单状态判断）
    @Query("SELECT COUNT(o) > 0 FROM OrderPO o JOIN o.orderItems oi " +
           "WHERE o.userId = :userId AND oi.productId = :productId AND o.status = 'SUCCESS'")
    boolean hasUserPurchasedProduct(@Param("userId") Long userId, @Param("productId") Long productId);
} 