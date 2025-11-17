package com.example.tomatomall.service;

import com.example.tomatomall.po.BannerPO;
import com.example.tomatomall.vo.BannerVO;

import java.util.List;

public interface BannerService {
    // 获取所有轮播图（包含书籍信息）
    List<BannerVO> getAllBanners();
    
    // 获取指定ID的轮播图
    BannerVO getBannerById(Long id);
    
    // 创建轮播图
    BannerVO createBanner(BannerPO bannerPO);
    
    // 更新轮播图
    BannerVO updateBanner(Long id, BannerPO bannerPO);
    
    // 删除轮播图
    void deleteBanner(Long id);
    
    // 更新轮播图的书籍
    BannerVO updateBannerBooks(Long id, List<Long> productIds);
} 