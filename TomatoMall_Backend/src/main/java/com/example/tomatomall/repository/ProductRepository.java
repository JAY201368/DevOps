package com.example.tomatomall.repository;

import com.example.tomatomall.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByTitleContaining(String keyword);
    List<Product> findByDescriptionContaining(String keyword);
    List<Product> findByCategoryContaining(String keyword);
} 