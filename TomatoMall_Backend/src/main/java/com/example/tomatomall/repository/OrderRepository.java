package com.example.tomatomall.repository;

import com.example.tomatomall.po.OrderPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderPO, Long> {
    List<OrderPO> findByUserId(Long userId);
}
