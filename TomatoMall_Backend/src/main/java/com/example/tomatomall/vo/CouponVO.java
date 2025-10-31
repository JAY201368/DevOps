package com.example.tomatomall.vo;

import com.example.tomatomall.po.CouponPO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Setter
@Getter
public class CouponVO {
    private Long id;

    @NotBlank(message = "促销券名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "折扣金额不能为空")
    @Min(value = 0, message = "折扣金额必须大于等于0")
    private BigDecimal discountAmount;

    @NotNull(message = "最低订单金额不能为空")
    @Min(value = 0, message = "最低订单金额必须大于等于0")
    private BigDecimal minOrderAmount;

    @NotNull(message = "开始日期不能为空")
    private String startDate;

    @NotNull(message = "结束日期不能为空")
    private String endDate;

    @NotNull(message = "总数量不能为空")
    @Min(value = 1, message = "总数量必须大于0")
    private Integer totalQuantity;

    private Integer remainingQuantity;

    private String createdAt;

    private Integer status;

    // 是否已过期
    private Boolean isExpired;

    // 是否已开始
    private Boolean isStarted;

    // 是否有效
    private Boolean isValid;

    public static CouponVO fromPO(CouponPO couponPO) {
        if (couponPO == null) {
            return null;
        }

        CouponVO couponVO = new CouponVO();
        couponVO.setId(couponPO.getId());
        couponVO.setName(couponPO.getName());
        couponVO.setDescription(couponPO.getDescription());
        couponVO.setDiscountAmount(couponPO.getDiscountAmount());
        couponVO.setMinOrderAmount(couponPO.getMinOrderAmount());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        couponVO.setStartDate(couponPO.getStartDate().format(formatter));
        couponVO.setEndDate(couponPO.getEndDate().format(formatter));
        
        couponVO.setTotalQuantity(couponPO.getTotalQuantity());
        couponVO.setRemainingQuantity(couponPO.getRemainingQuantity());
        couponVO.setCreatedAt(couponPO.getCreatedAt().format(formatter));
        couponVO.setStatus(couponPO.getStatus());
        
        LocalDateTime now = LocalDateTime.now();
        couponVO.setIsExpired(now.isAfter(couponPO.getEndDate()));
        couponVO.setIsStarted(now.isAfter(couponPO.getStartDate()));
        couponVO.setIsValid(couponPO.getStatus() == 1 && 
                            couponVO.getIsStarted() && 
                            !couponVO.getIsExpired() && 
                            couponPO.getRemainingQuantity() > 0);
        
        return couponVO;
    }
} 