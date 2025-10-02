package com.example.tomatomall.vo;

import com.example.tomatomall.po.StockpilePO;
import lombok.Data;
import lombok.Setter;
import lombok.Getter;

@Data
@Setter
@Getter
public class StockpileVO {
    private Long id;
    private Integer amount;
    private Integer frozen;
    private Long productId;

    public static StockpileVO fromPO(StockpilePO stockpilePO) {
        StockpileVO stockpileVO = new StockpileVO();
        stockpileVO.setId(stockpilePO.getId());
        stockpileVO.setAmount(stockpilePO.getAmount());
        stockpileVO.setFrozen(stockpilePO.getFrozen());
        stockpileVO.setProductId(stockpilePO.getProduct().getId());
        return stockpileVO;
    }
} 