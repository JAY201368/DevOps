package com.example.tomatomall.repository;

import com.example.tomatomall.po.ProductPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductPO, Long> {
} 