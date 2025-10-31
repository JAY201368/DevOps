package com.example.tomatomall.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Setter
@Getter
public class CouponDistributionRequestVO {
    @NotNull(message = "促销券ID不能为空")
    private Long couponId;
    
    // 可以是特定用户ID列表，或者为null表示按条件发放
    private List<Long> userIds;
    
    // 发放条件，可以是JSON字符串或者其他格式的条件描述
    private String distributionCondition;
    
    // 备注
    private String remark;
} 