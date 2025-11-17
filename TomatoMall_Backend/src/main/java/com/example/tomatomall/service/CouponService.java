package com.example.tomatomall.service;

import com.example.tomatomall.vo.CouponDistributionLogVO;
import com.example.tomatomall.vo.CouponDistributionRequestVO;
import com.example.tomatomall.vo.CouponVO;
import com.example.tomatomall.vo.UserCouponVO;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {
    
    // 创建促销券
    CouponVO createCoupon(CouponVO couponVO);
    
    // 更新促销券
    CouponVO updateCoupon(CouponVO couponVO);
    
    // 删除促销券
    void deleteCoupon(Long couponId);
    
    // 获取促销券详情
    CouponVO getCouponById(Long couponId);
    
    // 获取所有促销券
    List<CouponVO> getAllCoupons();
    
    // 获取有效的促销券
    List<CouponVO> getValidCoupons();
    
    // 发放促销券给指定用户
    List<UserCouponVO> distributeCoupons(CouponDistributionRequestVO requestVO, Long adminId);
    
    // 获取用户的促销券
    List<UserCouponVO> getUserCoupons(Long userId);
    
    // 获取用户未使用的促销券
    List<UserCouponVO> getUserUnusedCoupons(Long userId);
    
    // 获取促销券发放记录
    List<CouponDistributionLogVO> getCouponDistributionLogs(Long couponId);
    
    // 获取管理员的促销券发放记录
    List<CouponDistributionLogVO> getAdminCouponDistributionLogs(Long adminId);
    
    // 使用促销券
    UserCouponVO useCoupon(Long userCouponId, Long userId, Long orderId, BigDecimal orderAmount);
    
    // 获取用户促销券详情
    UserCouponVO getUserCouponById(Long userCouponId);
} 