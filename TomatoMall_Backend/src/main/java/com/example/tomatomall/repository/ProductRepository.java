package com.example.tomatomall.repository;

import com.example.tomatomall.po.ProductPO;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 商品数据访问接口
 * 
 * 提供商品相关的数据库操作，包括：
 * 1. 继承JpaRepository的基础CRUD操作
 * 2. 商品信息的增删改查
 * 
 * 主要用于推荐算法中获取所有商品信息，
 * 分析商品标签、评分等特征，为用户提供个性化推荐。
 * 
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
public interface ProductRepository extends JpaRepository<ProductPO, Long> {
} 