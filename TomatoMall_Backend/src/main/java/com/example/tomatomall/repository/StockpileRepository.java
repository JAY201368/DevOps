package com.example.tomatomall.repository;

import com.example.tomatomall.po.StockpilePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 库存数据访问接口
 * 
 * 提供库存相关的数据库操作，包括：
 * 1. 根据商品ID查询库存信息
 * 2. 继承JpaRepository的基础CRUD操作
 * 3. 库存数量的增删改查
 * 
 * 主要用于推荐算法中检查商品库存状态，
 * 确保只推荐有库存的商品给用户。
 * 
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
@Repository
public interface StockpileRepository extends JpaRepository<StockpilePO, Long> {
    
    /**
     * 根据商品ID查询库存信息
     * 
     * 查询指定商品的库存数量信息。
     * 用于推荐算法中过滤库存不足的商品，
     * 确保推荐的商品都是可购买的。
     * 
     * @param productId 商品ID，不能为null
     * @return 库存信息，如果商品没有库存记录则返回空的Optional
     */
    Optional<StockpilePO> findByProductId(Long productId);
} 