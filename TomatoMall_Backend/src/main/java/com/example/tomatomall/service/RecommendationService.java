package com.example.tomatomall.service;

import com.example.tomatomall.vo.ProductVO;
import java.util.List;

/**
 * 书籍推荐服务接口
 * 
 * 提供多种推荐策略的书籍推荐服务，包括：
 * 1. 基于用户历史购买记录的个性化推荐
 * 2. 基于平台热度的热门推荐
 * 3. 基于用户愿望单的相似书籍推荐
 * 
 * 推荐算法采用基于内容的推荐方法，通过分析书籍标签、
 * 用户偏好和评分等多维度因素，为用户提供精准的书籍推荐。
 * 
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
public interface RecommendationService {
    
    /**
     * 基于用户历史购买记录的标签分布推荐书籍
     * 
     * 分析用户的历史购买记录，提取购买书籍的标签信息，
     * 计算标签权重，推荐与用户偏好匹配的书籍。
     * 
     * 算法流程：
     * 1. 获取用户历史订单中的书籍
     * 2. 提取书籍标签并统计频率
     * 3. 计算标签权重（频率/总标签数）
     * 4. 基于标签匹配度计算推荐得分
     * 5. 考虑评分、库存、愿望单等因素
     * 6. 按得分排序返回推荐结果
     * 
     * @param userId 用户ID，用于获取用户历史购买记录
     * @param limit 推荐数量限制，控制返回的推荐书籍数量
     * @return 推荐书籍列表，按推荐得分降序排列
     * @throws IllegalArgumentException 当userId为null或limit小于等于0时
     */
    List<ProductVO> getPersonalizedRecommendations(Long userId, int limit);
    
    /**
     * 获取平台畅销书籍推荐
     * 
     * 基于书籍评分和库存状态，推荐平台最受欢迎的书籍。
     * 适用于新用户或未登录用户，提供通用的热门推荐。
     * 
     * 推荐标准：
     * 1. 书籍评分（5分制）
     * 2. 库存充足（有库存的书籍）
     * 3. 按评分降序排列
     * 
     * @param limit 推荐数量限制，控制返回的推荐书籍数量
     * @return 热门推荐书籍列表，按评分降序排列
     * @throws IllegalArgumentException 当limit小于等于0时
     */
    List<ProductVO> getPopularRecommendations(int limit);
    
    /**
     * 基于用户愿望单的标签分布推荐相似书籍
     * 
     * 分析用户愿望单中的书籍标签，推荐具有相似标签的书籍。
     * 帮助用户发现与已收藏书籍相似的新书籍。
     * 
     * 算法流程：
     * 1. 获取用户愿望单中的书籍
     * 2. 提取书籍标签并统计频率
     * 3. 计算标签权重（频率/总标签数，并放大3倍）
     * 4. 基于标签匹配度计算推荐得分
     * 5. 考虑评分、库存等因素
     * 6. 按得分排序返回推荐结果
     * 
     * @param userId 用户ID，用于获取用户愿望单信息
     * @param limit 推荐数量限制，控制返回的推荐书籍数量
     * @return 基于愿望单的推荐书籍列表，按推荐得分降序排列
     * @throws IllegalArgumentException 当userId为null或limit小于等于0时
     */
    List<ProductVO> getWishListBasedRecommendations(Long userId, int limit);
} 