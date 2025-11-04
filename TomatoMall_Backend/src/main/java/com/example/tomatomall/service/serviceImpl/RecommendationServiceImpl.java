package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.OrderItemPO;
import com.example.tomatomall.po.OrderPO;
import com.example.tomatomall.po.ProductPO;
import com.example.tomatomall.po.StockpilePO;
import com.example.tomatomall.repository.OrderRepository;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.repository.StockpileRepository;
import com.example.tomatomall.service.RecommendationService;
import com.example.tomatomall.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private StockpileRepository stockpileRepository;

    @Override
    public List<ProductVO> getPersonalizedRecommendations(Long userId, int limit) {
        // 1. 获取用户的历史订单
        List<OrderPO> userOrders = orderRepository.findByUserId(userId);
        
        // 如果用户没有购买记录，返回热门推荐
        if (userOrders == null || userOrders.isEmpty()) {
            return getPopularRecommendations(limit);
        }
        
        // 2. 分析用户购买的书籍标签分布
        Map<String, Integer> tagFrequency = new HashMap<>();
        int totalTags = 0;
        
        for (OrderPO order : userOrders) {
            // 只考虑已完成的订单
            if (!"completed".equalsIgnoreCase(order.getStatus())) {
                continue;
            }
            
            for (OrderItemPO item : order.getOrderItems()) {
                Long productId = item.getProductId();
                Optional<ProductPO> productOpt = productRepository.findById(productId);
                
                if (productOpt.isPresent()) {
                    ProductPO product = productOpt.get();
                    String tags = product.getTags();
                    
                    if (tags != null && !tags.isEmpty()) {
                        // 拆分多个标签（假设标签以逗号分隔）
                        String[] tagArray = tags.split(",");
                        for (String tag : tagArray) {
                            tag = tag.trim();
                            if (!tag.isEmpty()) {
                                tagFrequency.put(tag, tagFrequency.getOrDefault(tag, 0) + 1);
                                totalTags++;
                            }
                        }
                    }
                }
            }
        }
        
        // 如果没有提取到任何标签，返回热门推荐
        if (tagFrequency.isEmpty()) {
            return getPopularRecommendations(limit);
        }
        
        // 3. 计算每个标签的权重
        Map<String, Double> tagWeights = new HashMap<>();
        for (Map.Entry<String, Integer> entry : tagFrequency.entrySet()) {
            double weight = (double) entry.getValue() / totalTags;
            tagWeights.put(entry.getKey(), weight);
        }
        
        // 4. 按权重排序标签
        List<Map.Entry<String, Double>> sortedTags = new ArrayList<>(tagWeights.entrySet());
        sortedTags.sort(Map.Entry.<String, Double>comparingByValue().reversed());
        
        // 5. 获取用户已购买的商品ID列表（用于排除）
        Set<Long> purchasedProductIds = new HashSet<>();
        for (OrderPO order : userOrders) {
            for (OrderItemPO item : order.getOrderItems()) {
                purchasedProductIds.add(item.getProductId());
            }
        }
        
        // 6. 基于标签权重推荐书籍
        List<ProductPO> allProducts = productRepository.findAll();
        Map<Long, Double> productScores = new HashMap<>();
        
        for (ProductPO product : allProducts) {
            // 跳过用户已购买的商品
            if (purchasedProductIds.contains(product.getId())) {
                continue;
            }
            
            // 跳过库存不足的商品
            Optional<StockpilePO> stockpileOpt = stockpileRepository.findByProductId(product.getId());
            if (stockpileOpt.isEmpty() || stockpileOpt.get().getAmount() <= 0) {
                continue;
            }
            
            String productTags = product.getTags();
            if (productTags == null || productTags.isEmpty()) {
                continue;
            }
            
            // 计算商品得分（基于标签匹配和其他因素）
            double score = 0.0;
            String[] productTagArray = productTags.split(",");
            
            for (String tag : productTagArray) {
                tag = tag.trim();
                if (tagWeights.containsKey(tag)) {
                    score += tagWeights.get(tag);
                }
            }
            
            // 考虑评分因素（假设产品有rate字段表示评分，满分10分）
            if (product.getRate() != null) {
                score *= (0.5 + product.getRate() / 20.0); // 评分影响因子
            }
            
            productScores.put(product.getId(), score);
        }
        
        // 7. 按得分排序并选出前limit个
        List<Long> recommendedProductIds = productScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 8. 获取推荐商品详情
        List<ProductVO> recommendations = new ArrayList<>();
        for (Long productId : recommendedProductIds) {
            productRepository.findById(productId).ifPresent(product -> 
                recommendations.add(ProductVO.fromPO(product))
            );
        }
        
        return recommendations;
    }

    @Override
    public List<ProductVO> getPopularRecommendations(int limit) {
        // 获取评分最高且库存充足的书籍
        List<ProductPO> allProducts = productRepository.findAll();
        
        return allProducts.stream()
                // 确保库存充足
                .filter(product -> {
                    Optional<StockpilePO> stockpileOpt = stockpileRepository.findByProductId(product.getId());
                    return stockpileOpt.isPresent() && stockpileOpt.get().getAmount() > 0;
                })
                // 按评分排序
                .sorted(Comparator.comparing(ProductPO::getRate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .map(ProductVO::fromPO)
                .collect(Collectors.toList());
    }
} 