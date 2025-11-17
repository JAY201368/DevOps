package com.example.tomatomall.repository;

import com.example.tomatomall.po.CouponPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponRepository extends JpaRepository<CouponPO, Long> {
    
    // 查询有效的促销券（状态为有效、未过期、已开始、库存大于0）
    @Query("SELECT c FROM CouponPO c WHERE c.status = 1 AND c.startDate <= :now AND c.endDate >= :now AND c.remainingQuantity > 0")
    List<CouponPO> findValidCoupons(@Param("now") LocalDateTime now);
    
    // 查询指定管理员创建的促销券
    List<CouponPO> findByStatus(Integer status);
} 