package com.example.tomatomall.repository;

import com.example.tomatomall.po.OrderPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderPO, Long> {
    @Query("SELECT o FROM OrderPO o WHERE o.userId = :userId ORDER BY o.createTime DESC")
    List<OrderPO> findByUserId(@Param("userId") Long userId);

    @Query("SELECT o FROM OrderPO o WHERE o.status = 'PENDING' AND o.expireTime < :currentTime")
    List<OrderPO> findExpiredOrders(@Param("currentTime") Timestamp currentTime);
}
