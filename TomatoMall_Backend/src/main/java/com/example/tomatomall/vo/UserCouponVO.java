package com.example.tomatomall.vo;

import com.example.tomatomall.po.UserCouponPO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Setter
@Getter
public class UserCouponVO {
    private Long id;
    private Long userId;
    private Long couponId;
    private Integer isUsed;
    private String usedTime;
    private Integer orderId;
    private String createdAt;
    
    // 关联的优惠券信息
    private CouponVO coupon;
    
    // 用户信息
    private String username;
    private String name;

    public static UserCouponVO fromPO(UserCouponPO userCouponPO) {
        if (userCouponPO == null) {
            return null;
        }

        UserCouponVO userCouponVO = new UserCouponVO();
        userCouponVO.setId(userCouponPO.getId());
        userCouponVO.setUserId(userCouponPO.getUserId());
        userCouponVO.setCouponId(userCouponPO.getCouponId());
        userCouponVO.setIsUsed(userCouponPO.getIsUsed());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (userCouponPO.getUsedTime() != null) {
            userCouponVO.setUsedTime(userCouponPO.getUsedTime().format(formatter));
        }
        
        userCouponVO.setOrderId(userCouponPO.getOrderId());
        userCouponVO.setCreatedAt(userCouponPO.getCreatedAt().format(formatter));
        
        if (userCouponPO.getCoupon() != null) {
            userCouponVO.setCoupon(CouponVO.fromPO(userCouponPO.getCoupon()));
        }
        
        if (userCouponPO.getUser() != null) {
            userCouponVO.setUsername(userCouponPO.getUser().getUsername());
            userCouponVO.setName(userCouponPO.getUser().getName());
        }
        
        return userCouponVO;
    }
} 