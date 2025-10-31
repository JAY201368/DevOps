package com.example.tomatomall.repository;

import com.example.tomatomall.po.BannerPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<BannerPO, Long> {
    // 按显示顺序查询所有轮播图
    List<BannerPO> findAllByOrderByDisplayOrderAsc();
} 