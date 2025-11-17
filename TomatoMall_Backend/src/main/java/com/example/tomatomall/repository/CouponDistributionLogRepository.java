package com.example.tomatomall.repository;

import com.example.tomatomall.po.CouponDistributionLogPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponDistributionLogRepository extends JpaRepository<CouponDistributionLogPO, Long> {
    
    // 查询某个管理员的发放记录
    List<CouponDistributionLogPO> findByAdminId(Long adminId);
    
    // 查询某张促销券的发放记录
    List<CouponDistributionLogPO> findByCouponId(Long couponId);
    
    // 查询某个管理员发放某张促销券的记录
    List<CouponDistributionLogPO> findByAdminIdAndCouponId(Long adminId, Long couponId);
} 