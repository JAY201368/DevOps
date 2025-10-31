package com.example.tomatomall.vo;

import com.example.tomatomall.po.CouponDistributionLogPO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Data
@Setter
@Getter
public class CouponDistributionLogVO {
    private Long id;
    private Long adminId;
    private Long couponId;
    private String distributionTime;
    private Integer distributionCount;
    private String distributionCondition;
    private String remark;
    
    // 管理员信息
    private String adminUsername;
    private String adminName;
    
    // 优惠券信息
    private String couponName;

    public static CouponDistributionLogVO fromPO(CouponDistributionLogPO logPO) {
        if (logPO == null) {
            return null;
        }

        CouponDistributionLogVO logVO = new CouponDistributionLogVO();
        logVO.setId(logPO.getId());
        logVO.setAdminId(logPO.getAdminId());
        logVO.setCouponId(logPO.getCouponId());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        logVO.setDistributionTime(logPO.getDistributionTime().format(formatter));
        
        logVO.setDistributionCount(logPO.getDistributionCount());
        logVO.setDistributionCondition(logPO.getDistributionCondition());
        logVO.setRemark(logPO.getRemark());
        
        if (logPO.getAdmin() != null) {
            logVO.setAdminUsername(logPO.getAdmin().getUsername());
            logVO.setAdminName(logPO.getAdmin().getName());
        }
        
        if (logPO.getCoupon() != null) {
            logVO.setCouponName(logPO.getCoupon().getName());
        }
        
        return logVO;
    }
} 