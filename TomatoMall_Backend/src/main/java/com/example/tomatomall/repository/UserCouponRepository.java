package com.example.tomatomall.repository;

import com.example.tomatomall.po.UserCouponPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCouponPO, Long> {
    
    // 查询用户的所有促销券
    List<UserCouponPO> findByUserId(Long userId);
    
    // 查询用户未使用的促销券
    List<UserCouponPO> findByUserIdAndIsUsed(Long userId, Integer isUsed);
    
    // 查询用户是否已领取某张促销券
    Optional<UserCouponPO> findByUserIdAndCouponId(Long userId, Long couponId);
    
    // 查询某张促销券的领取记录
    List<UserCouponPO> findByCouponId(Long couponId);
    
    // 统计某张促销券的领取数量
    @Query("SELECT COUNT(uc) FROM UserCouponPO uc WHERE uc.couponId = :couponId")
    long countByCouponId(@Param("couponId") Long couponId);
    
    // 统计某张促销券的使用数量
    @Query("SELECT COUNT(uc) FROM UserCouponPO uc WHERE uc.couponId = :couponId AND uc.isUsed = 1")
    long countByCouponIdAndIsUsed(@Param("couponId") Long couponId);
    
    // 删除某张促销券的所有领取记录
    void deleteByCouponId(Long couponId);
} 