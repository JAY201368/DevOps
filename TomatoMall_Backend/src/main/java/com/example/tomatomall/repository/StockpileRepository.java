package com.example.tomatomall.repository;

import com.example.tomatomall.po.StockpilePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 库存数据访问接口
 */
@Repository
public interface StockpileRepository extends JpaRepository<StockpilePO, Long> {
    
    /**
     * 根据商品ID查询库存信息
     * @param productId 商品ID
     * @return 库存信息
     */
    Optional<StockpilePO> findByProductId(Long productId);
} 