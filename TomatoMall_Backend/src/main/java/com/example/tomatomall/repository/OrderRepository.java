package com.example.tomatomall.repository;

import com.example.tomatomall.po.OrderPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

/**
 * 订单数据访问接口
 * 
 * 提供订单相关的数据库操作，包括：
 * 1. 根据用户ID查询订单
 * 2. 查询过期订单
 * 3. 继承JpaRepository的基础CRUD操作
 * 
 * 主要用于推荐算法中获取用户历史购买记录，
 * 分析用户购买偏好和消费行为。
 * 
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
public interface OrderRepository extends JpaRepository<OrderPO, Long> {
    
    /**
     * 根据用户ID查询订单列表
     * 
     * 查询指定用户的所有订单，按创建时间降序排列。
     * 用于推荐算法中分析用户的历史购买行为。
     * 
     * @param userId 用户ID，不能为null
     * @return 用户订单列表，按创建时间降序排列，如果用户没有订单则返回空列表
     */
    @Query("SELECT o FROM OrderPO o WHERE o.userId = :userId ORDER BY o.createTime DESC")
    List<OrderPO> findByUserId(@Param("userId") Long userId);

    /**
     * 查询过期订单
     * 
     * 查询所有状态为PENDING且已过期的订单。
     * 用于定时任务中处理过期订单，恢复库存。
     * 
     * @param currentTime 当前时间戳，用于比较订单过期时间
     * @return 过期订单列表，如果没有过期订单则返回空列表
     */
    @Query("SELECT o FROM OrderPO o WHERE o.status = 'PENDING' AND o.expireTime < :currentTime")
    List<OrderPO> findExpiredOrders(@Param("currentTime") Timestamp currentTime);
}
