package com.example.tomatomall.service;

import com.example.tomatomall.vo.ProductVO;
import java.util.List;

/**
 * 书籍推荐服务接口
 */
public interface RecommendationService {
    
    /**
     * 基于用户历史购买记录的标签分布推荐书籍
     * @param userId 用户ID
     * @param limit 推荐数量限制
     * @return 推荐书籍列表
     */
    List<ProductVO> getPersonalizedRecommendations(Long userId, int limit);
    
    /**
     * 获取平台畅销书籍推荐
     * @param limit 推荐数量限制
     * @return 推荐书籍列表
     */
    List<ProductVO> getPopularRecommendations(int limit);
} 